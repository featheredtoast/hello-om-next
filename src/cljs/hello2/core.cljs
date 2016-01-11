(ns hello2.core
  (:require
   [goog.dom :as gdom]
   [om.next :as om :refer-macros [defui]]
   [om.dom :as dom]
   ))

(enable-console-print!)

(def app-state {:counters [{:count 0 :id 0} {:count 1 :id 1}]})

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
            (dom/div nil
                     (dom/span nil (str "Count: " count " id: " id))
                     (dom/button
                      #js {:onClick
                           (fn [e] (om/transact! this `[(counter/increment ~props) :counters]))}
                      "Click me!")))))
(def counter-view (om/factory Counter {:keyfn :id}))

(defui MyRoot
  static om/IQuery
  (query [this]
         (let [subquery (om/get-query Counter)]
           [{:counters subquery}]))
  Object
  (render [this]
          (println "root" (om/props this))
          (let [{:keys [counters]} (om/props this)]
            (dom/div nil
                     (str
                      "total: "
                      (->> counters
                           (map :count)
                           (reduce +)))
                     (dom/div nil "hi")
                     (dom/div nil
                              (map counter-view counters))))))

(def reconciler
  (om/reconciler
    {:state app-state
     :parser (om/parser {:read read :mutate mutate})}))

(om/add-root! reconciler
  MyRoot (gdom/getElement "app"))
