(ns cljgl.core
  (:require [cljgl.point :as p :refer [->Point2d]]
            [cljgl.vector :as v :refer [->Vector2d]])
  (:import (org.lwjgl.opengl Display DisplayMode GL11)
           (org.lwjgl.input Keyboard)))

(def context {:width 1024 
              :height 768})

(defn random-normalised-vector []
  (let [rand-fn #(- (rand) 0.5)]
    (v/normalise (->Vector2d (rand-fn) (rand-fn)))))

(defn random-colour []
  (let [rand-fn #(+ (* (rand) (- 1 %1)) %1)] [(rand-fn 0.2) (rand-fn 0.5) (rand-fn 0.8)]))

(defn random-bird
  [{width :width, height :height}]
  {:position (->Point2d (* (rand) width) (* (rand) height))
   :velocity (random-normalised-vector)
   :colour (random-colour)})

(def birds (atom (take 20 (repeatedly (partial random-bird context)))))

(defn init-2d-display [width height title]
  (Display/setDisplayMode (DisplayMode. width height))
  (Display/setTitle title)
  (Display/create)
  (GL11/glMatrixMode GL11/GL_PROJECTION)
  (GL11/glLoadIdentity)
  (GL11/glOrtho 0 width height 0 1 -1)
  (GL11/glMatrixMode GL11/GL_MODELVIEW)
  #_(GL11/glClearColor 0 0 0))

(defn shutdown-display [] (Display/destroy))

(defn map!
  [f coll]
  (let [t-coll (transient (vec coll))
        size (count coll)]
    (loop [idx 0]
      (if (>= idx size)
        (persistent! t-coll)
        (do (assoc! t-coll idx (f (nth t-coll idx)))
            (recur (inc idx)))))))

(defn bounce
  [{w :width, h :height} {{px :x, py :y} :position, {vx :x, vy :y} :velocity, :as bird}]
  (let [new-velocity (apply ->Vector2d
                            (cond
                              (< px 0) [(Math/abs vx) vy]
                              (< py 0) [vx (Math/abs vy)]
                              (> px w) [(- (Math/abs vx)) vy]
                              (> py h) [vx (- (Math/abs vy))]
                              :else [vx vy]))]
    (assoc bird :velocity new-velocity)))

(defn distance
  [{b1-position :position} {b2-position :position}]
  (p/distance b1-position b2-position))

(defn neighbours [max-distance birds bird]
  (filter (partial not= bird) (filter #(< (distance bird %) max-distance) birds)))

(defn aligned-vector [birds]
  (->> birds
       (map :velocity)
       (reduce v/add (->Vector2d 0.00001 0.00001))
       v/normalise))

(defn cohesed-vector [{{x :x, y :y} :position} birds]
  (let [[com-x com-y] (reduce (fn [[ax ay] {{bx :x, by :y} :position}] [(+ ax bx ) (+ ay by)]) [0 0] birds)
        bird-count (count birds)
        coh-x (/ com-x bird-count)
        coh-y (/ com-y bird-count)]
    (v/normalise (->Vector2d (- coh-x x) (- coh-y y)))))

(defn separated-vector [{{x :x, y :y} :position} birds]
  (let [[com-x com-y] (reduce (fn [[ax ay] {{bx :x, by :y} :position}] [(+ ax (- bx x) ) (+ ay (- by y))]) [0 0] birds)
        bird-count (count birds)
        sep-x (/ com-x bird-count)
        sep-y (/ com-y bird-count)]
    (v/normalise (->Vector2d (- sep-x) (- sep-y)))))

(def original-weight 50)
(def align-weight 0.05)
(def cohesion-weight 5.45)
(def separation-weight 4.75)

(defn flock [birds bird]
  (let [local-birds (neighbours 300 birds bird)
        aligned-vec (aligned-vector local-birds)
        cohesed-vec (cohesed-vector bird local-birds)
        separated-vec (separated-vector bird local-birds)] 
    (assoc bird :velocity (->> (v/scale (:velocity bird) original-weight)
                               (v/add (v/scale aligned-vec align-weight))
                               (v/add (v/scale cohesed-vec cohesion-weight))
                               (v/add (v/scale separated-vec separation-weight))
                               v/normalise))))

(defn new-position
  [context delta {{px :x, py :y} :position, {vx :x, vy :y} :velocity, :as bird}]
  (let [new-x (+ px (* delta vx))
        new-y (+ py (* delta vy))
        calculated-position (->Point2d new-x new-y)]
    (assoc bird :position calculated-position)))

(defn update-frame!
  [context delta birds]
  (swap! birds (fn [birds]
                 (let [update-fn (comp (partial bounce context)
                                       (partial flock birds)
                                       (partial new-position context (* delta 200)))]
                   (map! update-fn birds)))))

(defn get-current-time [] (System/nanoTime))

(defn update-thread [context birds]
  (loop [previous-time (get-current-time)]
    (let [this-time (get-current-time)
          delta (/ (- this-time previous-time) 1000000000)]
      (update-frame! context delta birds)
      (if-not (Thread/interrupted) (recur this-time)))))

(defn render-bird [{{x :x, y :y} :position, {vx :x, vy :y} :velocity, [r g b] :colour}]
  (GL11/glPushMatrix)
  (GL11/glTranslatef x y 0)
  (GL11/glRotatef (Math/toDegrees (Math/atan2 vx (- vy))) 0 0 1)
  (GL11/glBegin GL11/GL_TRIANGLES)
  (GL11/glColor3f r g b)
  (GL11/glVertex2f 0 -10)
  (GL11/glVertex2f -3 10)
  (GL11/glVertex2f 3 10)
  (GL11/glEnd)
  (GL11/glPopMatrix))

(defn render [context birds]
  (GL11/glClear GL11/GL_COLOR_BUFFER_BIT)
  (doseq [bird @birds] (render-bird bird))
  (Display/update)
  (Display/sync 60))

(defn run-display-thread
  [{width :width, height :height, :as context} birds]
  (future
    (init-2d-display width height "Clojure Flocking")
    (loop []
      (render context birds)
      (if-not (Keyboard/isKeyDown Keyboard/KEY_SPACE)
        (recur)
        (Display/destroy)))))

(defn run-update-thread
  [context birds]
  (future(update-thread context birds)))

(defn main []
  (let [display-thread (run-display-thread context birds)
        update-thread (run-update-thread context birds)]
    @display-thread
    (future-cancel update-thread)))

(defn -main [])
