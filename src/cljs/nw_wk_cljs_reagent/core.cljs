(ns nw-wk-cljs-reagent.core
    (:require [reagent.core :as reagent :refer [atom]]
              [secretary.core :as secretary :include-macros true]
              [goog.events :as events]
              [goog.history.EventType :as EventType])
    (:import goog.History))

;; * UTIlS
(def not-nil? (complement nil?))

;; * STATE
(defonce app-state (atom {:text "Hello, this is: "}))

(defn get-state [k & [default]]
  (clojure.core/get @app-state k default))

(defn put! [k v]
  (swap! app-state assoc k v))

;; * TESTS
;; (defrecord test-record [dev path chip])
;; nw_wk_cljs_test2.server.test-record
;; nw-wk-cljs-test2.server> (def record1 (apply ->test-record ["pci_5" "/pci@500" 1]))
;; #'nw-wk-cljs-test2.server/record1
;; nw-wk-cljs-test2.server> record1
;; #nw_wk_cljs_test2.server.test-record{:dev "pci_5", :path "/pci@500", :chip 1}
;; nw-wk-cljs-test2.server> (:dev record1)
;; "pci_5"

;; (def m {:first "Bob"
;;         :middle "J"
;;         :last "Smith"})

;; (let [{:keys [first last]} m]
;;   (println first)
;;   (println last))

;; (extend-type js/RegExp
;;   IFn
;;   (-invoke
;;    ([this s]
;;      (re-matches this s))))

;; (filter #"foo.*" ["foo" "bar" "foobar"])

;; * ARCH SPECIFIC DEFS
;; ** T5-8 DATA
;; 2 or 4 PM
;; each PM have 2 chips
;; each chip have 16 cores
;;  == 128 cids at all
;; each chip have 128 threads
;;  == 1024 vcpu, 8 vcpu per cid

;; ** ARCH DEFS
(def mach-defs
  {:t5-8-half
   {:name "T5-8 Half Populated"
    :chips {:chip0 {:id 0 :slot1 "pci@300" :slot3 "pci@340"}
            :chip1 {:id 1 :slot9 "pci@380" :slot11 "pci@3c0"}
            :chip6 {:id 6 :slot6 "pci@600" :slot8 "pci@640"}
            :chip7 {:id 7 :slot14 "pci@680" :slot16 "pci@680"}}
    :devs { "pci@300/pci@1/pci@0/pci@6" {:id "pci@300/pci@1/pci@0/pci@6" :slot  1 :chip 0 :sw 0}
            "pci@300/pci@1/pci@0/pci@c" {:id "pci@300/pci@1/pci@0/pci@c" :slot  2 :chip 0 :sw 2}
            "pci@340/pci@1/pci@0/pci@6" {:id "pci@340/pci@1/pci@0/pci@6" :slot  3 :chip 0 :sw 1}
            "pci@340/pci@1/pci@0/pci@c" {:id "pci@340/pci@1/pci@0/pci@c" :slot  4 :chip 0 :sw 1}            
            "pci@600/pci@1/pci@0/pci@e" {:id "pci@600/pci@1/pci@0/pci@e" :slot  5 :chip 2 :sw 0}
            "pci@600/pci@1/pci@0/pci@8" {:id "pci@600/pci@1/pci@0/pci@8" :slot  6 :chip 2 :sw 1}
            "pci@640/pci@1/pci@0/pci@e" {:id "pci@640/pci@1/pci@0/pci@e" :slot  7 :chip 2 :sw 2}
            "pci@640/pci@1/pci@0/pci@8" {:id "pci@640/pci@1/pci@0/pci@8" :slot  8 :chip 2 :sw 3}            
            "pci@380/pci@1/pci@0/pci@a" {:id "pci@380/pci@1/pci@0/pci@a" :slot  9 :chip 1 :sw 2}
            "pci@380/pci@1/pci@0/pci@4" {:id "pci@380/pci@1/pci@0/pci@4" :slot 10 :chip 1 :sw 2}
            "pci@3c0/pci@1/pci@0/pci@e" {:id "pci@3c0/pci@1/pci@0/pci@e" :slot 11 :chip 1 :sw 3}
            "pci@3c0/pci@1/pci@0/pci@8" {:id "pci@3c0/pci@1/pci@0/pci@8" :slot 12 :chip 1 :sw 3}
            "pci@600/pci@1/pci@0/pci@6" {:id "pci@600/pci@1/pci@0/pci@6" :slot 13 :chip 3 :sw 3}
            "pci@640/pci@1/pci@0/pci@6" {:id "pci@640/pci@1/pci@0/pci@6" :slot 14 :chip 3 :sw 3}
            "pci@680/pci@1/pci@0/pci@6" {:id "pci@680/pci@1/pci@0/pci@6" :slot 15 :chip 3 :sw 4}
            "pci@6c0/pci@1/pci@0/pci@6" {:id "pci@6c0/pci@1/pci@0/pci@6" :slot 16 :chip 3 :sw 4}}
    ;; TODO memory config for t5-8 half is wrong
    :mem {:0 {:start            "0x0" :end  "0x7FFFFFFFFFF"}
          :1 {:start  "0x80000000000" :end  "0xFFFFFFFFFFF"}
          :2 {:start "0x100000000000" :end "0x17FFFFFFFFFF"}
          :3 {:start "0x180000000000" :end "0x1FFFFFFFFFFF"}
          :4 {:start "0x200000000000" :end "0x27FFFFFFFFFF"}
          :5 {:start "0x280000000000" :end "0x2FFFFFFFFFFF"}
          :6 {:start "0x300000000000" :end "0x37FFFFFFFFFF"}
          :7 {:start "0x380000000000" :end "0x3FFFFFFFFFFF"}}}

   :t5-8-full
   {:name "T5-8 Fully Populated"
    :chips {:chip0 {:id 0 :slot1 "pci@300" :slot3 "pci@340"}
            :chip1 {:id 1 :slot9 "pci@380" :slot11 "pci@3c0"}
            :chip2 {:id 2 :slot2 "pci@400" :slot4 "pci@440"}
            :chip3 {:id 3 :slot10 "pci@480" :slot12 "pci@4c0"}
            :chip4 {:id 4 :slot5 "pci@500" :slot7 "pci@540"}
            :chip5 {:id 5 :slot13 "pci@580" :slot15 "pci@580"}
            :chip6 {:id 6 :slot6 "pci@600" :slot8 "pci@640"}
            :chip7 {:id 7 :slot14 "pci@680" :slot16 "pci@680"}}

    :devs { "pci@300" {:id "pci@300" :slot  1 :chip 0 :sw 0}
            "pci@340" {:id "pci@340" :slot  3 :chip 0 :sw 1}
            "pci@380" {:id "pci@380" :slot  9 :chip 1 :sw 2}
            "pci@3c0" {:id "pci@3c0" :slot 11 :chip 1 :sw 3}
            "pci@400" {:id "pci@400" :slot  2 :chip 2 :sw 0}
            "pci@440" {:id "pci@440" :slot  4 :chip 2 :sw 1}
            "pci@480" {:id "pci@480" :slot 10 :chip 3 :sw 2}
            "pci@4c0" {:id "pci@4c0" :slot 12 :chip 3 :sw 3}
            "pci@500" {:id "pci@500" :slot  5 :chip 4 :sw 1}
            "pci@540" {:id "pci@540" :slot  7 :chip 4 :sw 2}
            "pci@580" {:id "pci@580" :slot 13 :chip 5 :sw 3}
            "pci@5c0" {:id "pci@5c0" :slot 15 :chip 5 :sw 4}
            "pci@600" {:id "pci@600" :slot  6 :chip 6 :sw 1}
            "pci@640" {:id "pci@640" :slot  8 :chip 6 :sw 2}
            "pci@680" {:id "pci@680" :slot 14 :chip 7 :sw 3}
            "pci@6c0" {:id "pci@6c0" :slot 16 :chip 7 :sw 4}}
    
    :mem {:0 {:start            0x0 :end 0x7FFFFFFFFFF}
          :1 {:start  0x80000000000 :end 0xFFFFFFFFFFF}
          :2 {:start 0x100000000000 :end 0x17FFFFFFFFFF}
          :3 {:start 0x180000000000 :end 0x1FFFFFFFFFFF}
          :4 {:start 0x200000000000 :end 0x27FFFFFFFFFF}
          :5 {:start 0x280000000000 :end 0x2FFFFFFFFFFF}
          :6 {:start 0x300000000000 :end 0x37FFFFFFFFFF}
          :7 {:start 0x380000000000 :end 0x3FFFFFFFFFFF}}}})

(def t5-8-full (:t5-8-full mach-defs))

;; ** ARCH FUNCS
(defn dev-on-chip? [chip dev-struct]
  (let [[dev-name _] dev-struct
        dev-root (subs dev-name 0 7)]
    (if (contains? (:devs t5-8-full) dev-root)
      (if (= chip (:chip ((:devs t5-8-full) dev-root)))
        true))))
;; (keys (filter #(dev-on-chip? 1 %) dev-config))
;; ("pci@3c0/pci@1/pci@0/pci@e/network@0,2" "pci@3c0/pci@1/pci@0/pci@e/network@0" ...

(defn mem-on-chip? [chip mem-config-entry]
  (= (keyword (str chip)) (:cid (val mem-config-entry))))

(defn pa->cid [pa]
  (let [cid (filter #(<= (:start (val %)) pa (:end (val %))) (:mem t5-8-full))]
    (if (not-empty cid)
      (key (first cid))
      nil)))

(defn color-domain [ldom-config color-table]
  (let [domain-count (count (keys ldom-config))
        domain-colors (take domain-count (shuffle color-table))]
    (zipmap (keys ldom-config) domain-colors)))

;; TODO HORRIBLE move to cond or case matching
(defn clean-dev-alias [alias]
  (let [rio? (second (re-matches #"/SYS/(RIO.+)" alias))
        internal? (second (re-matches #"/SYS/(MB/.+)" alias))
        external? (second (re-matches #"/SYS/RCSA/(.+)" alias))]
    (if rio? (str rio?)
        (if internal? (str internal?)
            (if external? (str external?)
                alias)))))

;; * NODE-WEBKIT/JAVASCRIPT SPECIFIC
;; TODO if fs not available, get config from jetty server
(def fs (js/require "fs"))

(def site-location "public/config/")

(defn to-cljs-array [js-array]
  (for [i (range (alength js-array))]
    (aget js-array i)))

(defn read-site-list [site]
  (to-cljs-array (.readdirSync fs (.realpathSync fs (str site-location site)))))

(defn read-site-file-content [site file]
  (clojure.string/split-lines (str (.readFileSync fs (str site-location site "/" file)))))

(def nw-gui (js/require "nw.gui"))

(defn str->int [s]
  (js/parseInt s))

(def nw-win (.get (.-Window nw-gui)))
;; (.-height nw-win)

;; * LDOM CONFIG PARSING
;; TODO move all the state to atom
;; TODO profile parsing, it's too slow
(defn parse-entry [cid]
  (apply merge
    (for [entry cid]
      (let [el (clojure.string/split entry #"=")]
        {(keyword (first el)) (second el)}))))
;; (parse-entry (rest (clojure.string/split "|cid=0|cpuset=0,1,2,3,4,5,6,7" #"\|")))
;; {:cid "0", :cpuset "0,1,2,3,4,5,6,7"}

(defn parse-token [token blob]
  (apply merge (map #(let [entry (parse-entry (rest (clojure.string/split % #"\|")))]
                       (hash-map ((keyword token) entry) entry))
                 (filter #(re-matches (re-pattern (str "\\|" token "=.+")) %) blob))))
;; (parse-token "cid" pri-binding)
;; {"33" {:cid "33", :cpuset "264,265,266,267,268,269,270,271"}, ...
;; (sort (keys (parse-token "cid" pri-binding)))
;; ("0" "1" "32" "33")

;; TODO make colors distance function and maximize it in range of on chip
(def color-table
  ["Teal" "DeepSkyBlue" "MediumSpringGreen" "Aqua" "SteelBlue" "DarkOliveGreen" "OliveDrab" "LightSkyBlue" "LightBlue" "DarkGoldenRod" "IndianRed" "Chocolate" "Thistle" "LightCyan" "Khaki" "LightGoldenRodYellow" "LightPink" "NavajoWhite" "LemonChiffon"])

(defn ldom-bindings [site]
  (filter #(not-nil? (first %))
    (apply merge
      {"unassigned" "_usr_sbin_ldm_ls-devices_-p"}
      (mapv #(let [b (re-matches #".+bindings_-p_(.+)" %)]
               {(second b) (first b)})
            (read-site-list site)))))
;; (ldom-bindings "site0")
;; (["t58-12023-o6" "_usr_sbin_ldm_ls-bindings_-p_t58-12023-o6"] ...

(defn read-ldom-config [site]
  (into {}
    (for [[domain-name domain-file] (ldom-bindings site)
          :let [domain-content (read-site-file-content site domain-file)]]
      {(keyword domain-name) {:cids (parse-token "cid" domain-content)
                              :devs (parse-token "dev" domain-content)
                              :mem (merge
                                    (parse-token "pa" domain-content)
                                    (parse-token "ra" domain-content))}})))
;; (:cids (ldom-config "t58-12023-o6"))
;; {"41" {:cid "41", :cpuset "328,329,330,331,332,333,334,335"}, ...

;; (quick-bench (read-ldom-config site))
;; Evaluation count : 48 in 6 samples of 8 calls.
;;              Execution time mean : 14.723262 ms
;;     Execution time std-deviation : 1.278626 ms
;;    Execution time lower quantile : 13.477049 ms ( 2.5%)
;;    Execution time upper quantile : 16.630336 ms (97.5%)
;; Overhead used : 9.905376 ns

;; with instaparse:
;; (def parse
;;      (inst/parser
;;         "<config> = <'VERSION'> version nl section+
;;          <ws> = <#'\\s+'>
;;          <nl> = <#'[\\s*\n]+'>
;;          version = #'[0-9\\.]+'
;;          section = keyword <delim> (param delim?)+
;;          <keyword> = #'[A-Z]+'
;;          <param> = var <'='> value?
;;          var = #'[a-z0-9\\-\\_]+'
;;          value = #'[a-z0-9\\.\\-\\:\\,]+'
;;          <delim> = <'|'>"
;; 	:auto-whitespace :standard))

;; Evaluation count : 6 in 6 samples of 1 calls.
;;       Execution time sample mean : 1.328034 sec
;;              Execution time mean : 1.328233 sec
;; Execution time sample std-deviation : 21.225902 ms
;;     Execution time std-deviation : 21.572560 ms
;;    Execution time lower quantile : 1.304001 sec ( 2.5%)
;;    Execution time upper quantile : 1.362312 sec (97.5%)
;;                    Overhead used : 9.830718 ns

;; Found 2 outliers in 6 samples (33.3333 %)
;; 	low-severe	 1 (16.6667 %)
;; 	low-mild	 1 (16.6667 %)
;;  Variance from outliers : 13.8889 % Variance is moderately inflated by outliers

;; TODO this is unbound resources. move to other readings
(defn read-dev-config [site]
  (let [dev-content (read-site-file-content site "_usr_sbin_ldm_ls-io_-p")]
    (parse-token "dev" dev-content)))

(defn get-cid-config [config]
  (apply merge
    (flatten
     (for [[domain-name domain-config] config]
       (for [[cid-id cid-value] (:cids domain-config)]
         {cid-id (assoc cid-value :domain domain-name)})))))

(defn get-mem-config [config]
  (apply merge
    (flatten
     (for [[domain-name domain-config] config]
       (for [[memseg-name memseg-value] (:mem domain-config)
             :let [pa (:pa memseg-value)]]
         {pa {:pa pa :cid (pa->cid pa) :size (:size memseg-value) :domain domain-name}})))))
;; (get-mem-config ldom-config)
;; {"0xc30000000" {:pa "0xc30000000", :cid :0, :size "45902462976",:domain :t58-12023-o2},
;;  "0x103300000000" {:pa "0x103300000000", :cid :2, :size "55834574848", :domain :unassigned},

;; * INTERFACE
;; ** LISTERS
(defn lister [items]
  [:div
   (for [item items]
     ^{:key item} [:div {:style {:background-color "Aquamarine"}}
                   item])])

;; TODO get # of cores from config
(defn lister-domains [items config]
  [:div 
   (for [item items
         :let [color ((:colors config) item)
               cores (count (:cids (item (:domains config))))
               memory (reduce + (map #(str->int (:size (val %))) (:mem (item (:domains config)))))]]
     ^{:key item} [:div {:style {:background-color color :margin-bottom "3px"}}
                   [:div {:style {:font-weight "bold"}} (name item)]
                   [:div {:style {:text-align "right"}}
                    (str "cores: " cores " vcpu: " (* cores 8))]
                   [:div {:style {:text-align "right"}}
                    (str "memory: " (/ memory 1073741824) " GB")]])])

(defn lister-cid [cids config]
  [:div 
   (for [cid cids
         :let [color ((:colors config) (:domain ((:cids config) (str cid))))]]
     ^{:key cid} [:div {:style {:background-color color}}
                  "cid " cid])])

(defn lister-mem [items config]
  [:div
   (for [item items
         :let [color ((:colors config) (:domain (val item)))]]
     ^{:key item} [:div {:style {:background-color color}}
                   (/ (:size (val item)) 1073741824) " GB"])])

;; TODO device path in popup hint
(defn lister-io [items config]
  [:div 
   (for [item items
         :let [color ((:colors config) (keyword (:domain ((:devs config) item))))]]
     ^{:key item} [:div {:style {:background-color color}}
                   (clean-dev-alias (:alias ((:devs config) item)))])])

;; ** COMPONENTS
;; TODO get number of cids per chip from, well, somewhere
(defn cid-comp [chip config]
  (let [core-per-chip 16]
    [:div
     [:span {:style {:font-weight "bold"}} "Cores"]
     [lister-cid
      (range (* chip core-per-chip) (* (inc chip) core-per-chip))
      config]]))

(defn memory-comp [chip config]
  [:div {:style {:padding-bottom "10px"}}
   ;;[:span {:style {:font-weight "bold"}} "Memory"]
   [:span.mem "Memory"]
    [lister-mem
     (filter #(mem-on-chip? chip %) (:mem config))
     config]])

(defn io-comp [chip config]
  [:div
   {:style {:position "relative" :bottom "0px"}}
   [:span {:style {:font-weight "bold"}} "I/O"]
   [lister-io
    (sort
     (keys
      (remove #(re-matches #"pci@..." (first %))
        (filter #(dev-on-chip? chip %) (:devs config)))))
    config]])

(defn chip-comp [chip-num config]
  [:div.col-xs-3
   [:h5 (str "Chip " chip-num)]
    [:div.row
     [:div.col-xs-4 {:style {:padding-right "5px"}} [cid-comp chip-num config]]
     [:div.col-xs-8 {:style {:padding-left "5px"}} [memory-comp chip-num config]
      [io-comp chip-num config]]]])

;; ** MAIN
(defn main-page []
  (let [ldom-config (read-ldom-config "site0")
        dev-config (read-dev-config "site0")
        cid-config (get-cid-config ldom-config)
        mem-config (get-mem-config ldom-config)
        colors (color-domain ldom-config color-table)
        config {:domains ldom-config :cids cid-config :devs dev-config :mem mem-config :colors colors}
        chip-ids (map #(:id %) (vals (:chips t5-8-full)))]
    [:div.container
     [:div.row
      [:div.col-xs-12
       [:h3 "Site: " "Somebank test" " | System: " (:name t5-8-full)]]]
     [:div.row
      [:div.col-xs-10
       (map #(vector :div.row {:style {:padding-bottom "25px"}} %)
         (partition 4 (mapv #(chip-comp % config) chip-ids)))]
      [:div.list-domain.col-xs-2 [:h3 "Domains"]
       (lister-domains (sort (keys ldom-config)) config)]]]))

;; * ROUTES (not used - TODO)
(secretary/set-config! :prefix "#")

(secretary/defroute "/" []
  (put! :current-page :page1))

(secretary/defroute "/page2" []
  (put! :current-page :page2))

;; * INITIALIZE APP
(defn init! []
  (set! (. js/document -title) "LDOM visualizer")
  (reagent/render-component [main-page] (.getElementById js/document "app")))
