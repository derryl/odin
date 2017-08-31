(ns odin.profile.subs
  (:require [odin.profile.db :as db]
            [odin.profile.payments.subs]
            [re-frame.core :refer [reg-sub]]))


(reg-sub
 ::profile
 (fn [db _]
   (db/path db)))

(reg-sub
  :profile/account
  :<- [::profile]
  (fn [profile _]
    (:account profile)))
