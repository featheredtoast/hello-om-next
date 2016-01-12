(ns hello2.core
  (:require
   [goog.dom :as gdom]
   [om.next :as om :refer-macros [defui]]
   [om.dom :as dom]
   ))

(enable-console-print!)

(def app-state (->> (range 0 1000)
                    (map #(assoc {:count 1} :id %))
                    vec
                    (assoc {} :counters)))

(println app-state)

(defn get-counters [state key]
  (let [st @state]
    (into [] (map #(get-in st %)) (get st key))))

(defmulti read om/dispatch)

(defmethod read :counters
  [{:keys [state] :as env} key params]
  {:value (get-counters state key)})

(defmulti mutate om/dispatch)

(defmethod mutate 'counter/increment
  [{:keys [state]} _ {:keys [id]}]
  {:action
   (fn []
     (swap! state update-in [:counter/by-id id :count] inc))})

(defui Counter
  static om/Ident
  (ident [this {:keys [id]}]
         [:counter/by-id id])
  static om/IQuery
  (query [this]
         '[:id :count])
  Object
  (render [this]
          (let [{:keys [count id] :as props} (om/props this)]
            (println "rendering counter: " id)
            (dom/div nil
                     (dom/span nil (str "Id: " id " count: " count))
                     (dom/button
                      #js {:onClick
                           (fn [e] (om/transact! this `[(counter/increment ~props)]))}
                      "+")))))
(def counter-view (om/factory Counter {:keyfn :id}))

(defui Totals
  Object
  (render [this]
          (println "rendering total")
          (dom/div nil "total counters:"
                   (->> (om/props this)
                        (map :count)
                        (reduce +)))))
(def total-view (om/factory Totals))

(defui MyRoot
  static om/IQuery
  (query [this]
         (let [subquery (om/get-query Counter)]
           [{:counters subquery}]))
  Object
  (render [this]
          (println "rendering root" (om/props this))
          (let [{:keys [counters]} (om/props this)]
            (dom/div nil
                     (total-view counters)
                     (dom/div nil
                              (map counter-view counters))))))

(def reconciler
  (om/reconciler
    {:state app-state
     :parser (om/parser {:read read :mutate mutate})}))

(om/add-root! reconciler
  MyRoot (gdom/getElement "app"))
