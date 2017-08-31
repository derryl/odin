(ns odin.profile.events
  (:require [odin.profile.db :as db]
            [odin.profile.payments.events]
            [odin.profile.payments.sources.events]
            [odin.routes :as routes]
            [re-frame.core :refer [reg-event-db
                                   reg-event-fx
                                   path]]
            [toolbelt.core :as tb]))


(defmethod routes/dispatches :profile/membership []
  [[:profile.account/fetch 285873023223075]])


(reg-event-fx
 :profile.account/fetch
 [(path db/path)]
 (fn [{:keys [db]} [_ account-id]]
   (tb/log "it happened")
   {:graphql {:query
              [[:account {:id account-id}
                [:id :name :email :phone :first_name :last_name
                 ;;[:property]
                 [:active_license [:id :rate :starts :ends :term :status
                                   [:payments [:paid_on :amount]]
                                   [:property [:id :name]]
                                   [:unit     [:id :number]]]]
                 [:deposit [:id :amount :due :amount_paid :amount_remaining :amount_pending]]]]]
              :on-success [:profile.account.fetch/success]
              :on-failure [:profile.account.fetch/failure]}}))


(reg-event-db
 :profile.account.fetch/success
 [(path db/path)]
 (fn [db [_ response]]
   (let [account (get-in response [:data :account])]
     (tb/log account)
     (assoc-in db [:account] account))))

(reg-event-db
 :profile.account.fetch/failure
 [(path db/path)]
 (fn [db [_ response]]
   (tb/log "failed" response)
   db))
