(ns cljgl.core
  (:require [clojure.core.async :as a])
  (:import (org.lwjgl.opengl Display DisplayMode GL11)
           (org.lwjgl.input Keyboard)))

(def context {:width 640
              :height 480})

(defn vector-length
  [[x y]]
  (Math/sqrt (+ (* x x) (* y y))))

(defn vector-normalise
  [[x y :as v]]
  (let [length (vector-length v)]
    [(/ x length) (/ y length)]))

(defn random-normalised-vector
  []
  (let [rand-fn #(- (rand))]
    (vector-normalise [(rand-fn) (rand-fn)])))

(defn random-bird
  [{width :width, height :height}]
  {:position [(* (rand) width) (* (rand) height)]
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

(defn shutdown-display []
  (Display/destroy))

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
  [{width :width, height :height} {[px py] :position, [vx vy] :velocity, :as bird}]
  (let [new-velocity (cond
                       (> 0 px) [(Math/abs vx) vy]
                       (> 0 py) [vx (Math/abs vy)]
                       (< width px) [(- (Math/abs vx)) vy]
                       (< height py) [vx (- (Math/abs vy))]
                       :else [vx vy])]
    (assoc bird :velocity new-velocity)))

(defn new-position
  [context delta {[px py] :position, [vx vy] :velocity, :as bird}]
  (let [calculated-position [(+ px (* delta vx)) (+ py (* delta vy))]]
    (assoc bird :position calculated-position)))

(defn update-frame! [context delta birds]
  (swap! birds (fn [birds]
                 (let [update-fn (comp (partial bounce context)
                                       (partial new-position context (* delta 200)))]
                   (map! update-fn birds)))))

(defn get-current-time [] (System/nanoTime))

(defn update-thread [context birds]
  (loop [previous-time (get-current-time)]
    (let [this-time (get-current-time)
          delta (/ (- this-time previous-time) 1000000000)]
      (update-frame! context delta birds)
      (recur this-time))))

(defn render-bird [{[x y] :position, [vx vy] :velocity, [r g b] :colour}]
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
