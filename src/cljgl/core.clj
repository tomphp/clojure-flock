(ns cljgl.core
  (:require [clojure.core.async :as a]
            [cljgl.point :as p :refer [->Point2d]]
            [cljgl.vector :as v :refer [->Vector2d]])
  (:import (org.lwjgl.opengl Display DisplayMode GL11)
           (org.lwjgl.input Keyboard)))

(def context {:width 640
              :height 480})

(defn random-normalised-vector []
  (let [rand-fn #(- (rand) 0.5)]
    (v/normalise (->Vector2d (rand-fn) (rand-fn)))))

(defn random-colour [] [(rand) (rand) (rand)])

(defn random-bird
  [{width :width, height :height}]
  {:position (->Point2d (* (rand) width) (* (rand) height))
   :velocity (random-normalised-vector)
   :colour [(rand) (rand) (rand)]})

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
  (filter #(< (distance bird %) max-distance) birds))

(defn aligned-vector [vectors]
  (->> vectors
       (reduce v/add (->Vector2d 0.00001 0.00001))
       v/normalise))

(defn cohesed-vector [birds {{x :x, y :y} :position}] nil)

(defn flock [birds bird]
  (assoc bird :velocity (->> (neighbours 50 birds bird)
                             (map :velocity)
                             aligned-vector

                             (v/add (:velocity bird))
                             v/normalise)))

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
      (recur this-time))))

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
  (a/thread
    (init-2d-display width height "Clojure Flocking")
    (loop []
      (render context birds)
      (if-not (Keyboard/isKeyDown Keyboard/KEY_SPACE)
        (recur)
        (Display/destroy)))))

(defn run-update-thread
  [context birds]
  (a/thread (update-thread context birds)))

(defn main []
  (let [display-thread (run-display-thread context birds)
        update-thread (run-update-thread context birds)
        threads [display-thread update-thread]]
    (a/alts!! threads)))

(defn -main [])
