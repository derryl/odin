(ns odin.profile.membership.subs
  (:require [odin.profile.membership.db :as db]
            [re-frame.core :refer [reg-sub subscribe]]
            [toolbelt.core :as tb]))

(reg-sub
 ::membership
 (fn [db _]
   (db/path db)))


(reg-sub
 :member/license
 :<- [::membership]
 (fn [db _]
   (:license db)))


(reg-sub
 :member/deposit
 :<- [::membership]
 (fn [db _]
   (:deposit db)))


(reg-sub
 :member.license/loading?
 :<- [:member/license]
 :<- [:loading? :member.license/fetch]
 (fn [[license loading]]
   (and (empty? license) loading)))


(reg-sub
 :member/rent-payments
 :<- [::membership]
 (fn [db [_ {:keys [status]}]]
   (let [payments (get-in db [:license :payments])]
     (if (some? status)
       (filter #(= status (:status %)) payments)
       payments))))


(reg-sub
 :member.deposit/payment
 :<- [:member/deposit]
 (fn [deposit _]
   {:id          (:id deposit)
    :amount      (:amount_remaining deposit)
    :due         (:due deposit)
    :description "Remainder of your security deposit."
    :type        :deposit}))
