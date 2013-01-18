(ns nsfw.chart
  (:require [goog.ui.ServerChart]
            [nsfw.util :as util]))

(defn apply-data-set
  "Apply `data-set` to `chart`. Data set can be either a seq, in
   which case it will be added to `chart` with default styling options,
   or a map containing styling options in addition to a `:data` key."
  [chart data-set]
  (let [data (if (or (vector? data-set)
                     (list? data-set))
               data-set
               (:data data-set)p)
        color (:color data-set "000000")
        legend (:legend data-set)]
    (if legend
      (.addDataSet chart (clj->js data) color legend)
      (.addDataSet chart (clj->js data) color))))

(def default-chart-opts {:bg "ffffff"
                         :title nil
                         :left-labels nil})

(defn chart-types [chart-type]
  (let [ct goog.ui.ServerChart.ChartType]
    (get {:bar ct/BAR
          :clock ct/CLOCK
          :concentric-pie ct/CONCENTRIC_PIE
          :filledline ct/FILLEDLINE
          :googleometer ct/GOOGLEOMETER
          :horizontal-grouped-bar ct/HORIZONTAL_GROUPED_BAR
          :horizontal-stacked-bar ct/HORIZONTAL_STACKED_BAR
          :map ct/MAP
          :mapusa ct/MAPUSA
          :mapworld ct/MAPWORLD
          :pie ct/PIE
          :pie3d ct/PIE3D
          :radar ct/RADAR
          :scatter ct/SCATTER
          :sparkline ct/SPARKLINE
          :venn ct/VENN
          :vertical-grouped-bar ct/VERTICAL_GROUPED_BAR
          :vertical-stacked-bar ct/VERTICAL_STACKED_BAR
          :xy-line ct/XYLINE}
         chart-type
         goog.ui.ServerChart.ChartType/LINE)))

(defn chart-defaults [chart-type]
  (get {:sparkline {:width 100
                    :height 50}}
       chart-type
       {:width 500
        :height 200}))

(defn chart
  "Optionally set :with-chart to a function taking one parameter (chart),
   which allows you to manually configure the chart object. See "
  [chart-type data-sets chart-opts]
  (let [chart-opts (merge default-chart-opts chart-opts)
        {:keys [width height bg title min max left-labels]}
        (merge (chart-defaults chart-type)
               chart-opts)
        chart (goog.ui.ServerChart. (chart-types chart-type) width height)]
    (doseq [data-set data-sets]
      (apply-data-set chart data-set))
    (when bg (.setBackgroundFill chart (clj->js [{:area "bg" :color bg}])))
    (when title (.setTitle chart title))
    (when min (.setMinValue chart min))
    (when max (.setMaxValue chart max))
    (when left-labels (.setLeftLabels chart (clj->js left-labels)))
    (.createDom chart)
    (.getElement chart)))

(defn chart-fn [type]
  (fn [data-sets chart-opts]
    (chart type data-sets chart-opts)))

(def bar (chart-fn :bar))
(def clock (chart-fn :clock))
(def concentric-pie (chart-fn :concentric-pie))
(def filledline (chart-fn :filledline))
(def googleometer (chart-fn :googleometer))
(def horizontal-grouped-bar (chart-fn :horizontal-grouped-bar))
(def horizontal-stacked-bar (chart-fn :horizontal-stacked-bar))
(def line (chart-fn :line))
(def map (chart-fn :map))
(def mapusa (chart-fn :mapusa))
(def mapworld (chart-fn :mapworld))
(def pie (chart-fn :pie))
(def pie3d (chart-fn :pie3d))
(def radar (chart-fn :radar))
(def scatter (chart-fn :scatter))
(def sparkline (chart-fn :sparkline))
(def venn (chart-fn :venn))
(def vgb (chart-fn :vertical-grouped-bar))
(def vsb (chart-fn :vertical-stacked-bar))
(def xy-line (chart-fn :xy-line))