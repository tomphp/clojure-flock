(ns cljgl.core
  (:import (org.lwjgl Version)
           (org.lwjgl.opengl GL GL11)
           (org.lwjgl.system MemoryUtil)
           (org.lwjgl.glfw GLFW GLFWErrorCallback))
  (:require [clojure.core.async :refer [<!! chan]]))

(defonce error-callback (GLFWErrorCallback/createPrint (System/err)))

(defn init []
  (GLFW/glfwSetErrorCallback error-callback)
  (if (not= (GLFW/glfwInit) GLFW/GLFW_TRUE)
    (throw "Unable to initialize GLFW"))
  (GLFW/glfwDefaultWindowHints)
  (GLFW/glfwWindowHint GLFW/GLFW_VISIBLE GLFW/GLFW_FALSE) ; GLFW_FALSE
  (GLFW/glfwWindowHint GLFW/GLFW_RESIZABLE GLFW/GLFW_TRUE) ; GLFW_TRUE
  (let [width 300
        height 300
        window (GLFW/glfwCreateWindow width height "Hello World!" 0 0)
        ; key callback
        vidmode (GLFW/glfwGetVideoMode (GLFW/glfwGetPrimaryMonitor))
        top (/ (- (.height vidmode) height) 2) 
        left (/ (- (.width vidmode) width) 2)]
    (if (nil? window) (throw "Failed to create the GLFW window"))
    (GLFW/glfwSetWindowPos window top left)
    (GLFW/glfwMakeContextCurrent window)
    (GLFW/glfwSwapInterval 1)
    (GLFW/glfwShowWindow window)
    ))

(defn -main []
  (println (str "Hello LWJGL " (Version/getVersion) "!"))
  (try
    (init)
    (loop []
      (GL/createCapabilities)
      (recur))
    (finally
      (GLFW/glfwTerminate)
      (.release error-callback))))
