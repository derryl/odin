(ns odin.core
  (:require [odin.events]
            [odin.routes :as routes]
            [odin.l10n :as l10n]
            [odin.subs]
            [odin.graphql]
            [odin.content :as content]
            [odin.profile.views]
            [odin.account.list.views]
            [odin.account.entry.views]
            [odin.utils.formatters :as formatters]
            [day8.re-frame.http-fx]
            [antizer.reagent :as ant]
            [cljsjs.moment]
            [clojure.string :as string]
            [goog.dom :as gdom]
            [re-frame.core :as rf :refer [dispatch subscribe]]
            [reagent.core :as r]
            [ajax.core :refer [GET]]
            [toolbelt.core :as tb]))


(enable-console-print!)

;; =============================================================================
;; Layout
;; =============================================================================


(defn burger []
  [:div.navbar-burger.burger
   {:on-click #(dispatch [:menu/toggle])}
   [:span] [:span] [:span]])


(defn brand []
  [:div.navbar-brand
   [:a.navbar-item.brand-logo {:href "/"} "Starcity"]
   (burger)])


(defn navbar-menu-item
  [role {:keys [feature titles uri]}]
  (let [root (subscribe [:route/root])]
    [:a.navbar-item {:href  uri
                     :class (when (= feature @root) "is-active")}
     (l10n/translate (keyword (name feature) role))]))


(defn navbar-menu []
  (let [menu-items (subscribe [:menu/items])
        role       (subscribe [:config/role])]
    ;;(tb/log @menu-items)
    [:div.navbar-start
     (doall
      (map-indexed
       #(with-meta [navbar-menu-item @role %2] {:key %1})
       @menu-items))]))


(defn navbar []
  (let [menu-showing (subscribe [:menu/showing?])
        account      (subscribe [:account])]
    [:nav.navbar
     (brand)
     [:div.navbar-menu {:class (when @menu-showing "is-active")}
      [navbar-menu]
      [:div.navbar-end
       [:a.navbar-item.hoverable
        {:href (routes/path-for :profile/membership)}
        [:div.flexbox.has-pointer
         [ant/avatar (formatters/initials (:name @account))]
         [:span.valign.pad-left
          (:name @account)]]]]]]))


(defn error-view []
  [:section.hero.is-fullheight
   [:div.hero-body
    [:div.container.has-text-centered
     [:h1.is-2.title "Error!"]
     [ant/icon {:type  "close-circle"
                :style {:font-size 48 :color "red"}}]
     [:div {:style {:margin-top 24}}
      [:p.is-5.subtitle "Please check your network connection and reload this page."]]]]])


(defn layout []
  (let [curr-route (subscribe [:route/current])
        error      (subscribe [:config/error?])]
    (if @error
      [error-view]
      [:div.container
       [navbar]
       [:section.section.root-section
        [:div.columns
         [:div.column
          [content/view @curr-route]]]]])))


;; =============================================================================
;; App Entry
;; =============================================================================


(defn render []
  (r/render
   [ant/locale-provider {:locale (ant/locales "en_US")}
    [layout]]
   (gdom/getElement "odin")))


(defn ^:export run []
  (GET "/api/config"
       :handler (fn [config]
                  ;;(tb/log "CONFIG" config)
                  (rf/dispatch-sync [:app/init config])
                  (routes/hook-browser-navigation! config)
                  (render))
       :error-handler (fn [res]
                        (tb/error res)
                        (rf/dispatch-sync [:app/init-error])
                        (render))))
