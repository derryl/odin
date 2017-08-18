(ns odin.profile.payments.events
  (:require [odin.profile.payments.db :as db]
            [odin.routes :as routes]
            [re-frame.core :refer [reg-event-db
                                   reg-event-fx
                                   path]]
            [toolbelt.core :as tb]))


(defmethod routes/dispatches :profile.payment/history [route]
  [[:profile.payments/fetch (get-in route [:requester :id])]])


(reg-event-fx
 :profile.payments/fetch
 [(path db/path)]
 (fn [{:keys [db]} [_ account-id]]
   {:db      (assoc-in db [:loading :payments/list] true)
    :graphql {:query
              [[:payments {:account account-id}
                [:id :method :for :autopay :amount :status
                 :pstart :pend :paid_on [:source [:id :name :type :last4]]]]]
              :on-success [:profile.payments.fetch/success]
              :on-failure [:profile.payments.fetch/failure]}}))


(reg-event-db
 :profile.payments.fetch/success
 [(path db/path)]
 (fn [db [_ response]]
   (tb/log response)
   (let [payments (get-in response [:data :payments])]
     (-> (assoc db :payments payments)
         (assoc-in [:loading :payments/list] false)))))


(reg-event-fx
 :profile.payments.fetch/failure
 [(path db/path)]
 (fn [{:keys [db]} [_ response]]
   (tb/log response)
   {:db       (assoc-in db [:loading :payments/list] false)
    :dispatch [:graphql/notify-errors! response]}))