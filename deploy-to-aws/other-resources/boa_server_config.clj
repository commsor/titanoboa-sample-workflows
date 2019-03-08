(in-ns 'titanoboa.server)
(log/info "Hello, I am core.async server-config and I am being loaded...")
(defonce archival-queue-local (clojure.core.async/chan (clojure.core.async/dropping-buffer 1024)))
(alter-var-root #'server-config
                (constantly {:systems-catalogue
                                               {:core  {:system-def #'titanoboa.system.local/local-core-system
                                                        :worker-def #'titanoboa.system.local/local-worker-system
                                                        :worker-count 1
                                                        :autostart  true}
                                                :archival-system {:system-def #'titanoboa.system.local/archival-system
                                                                  :autostart  true}
                                                :auth-system {:system-def #'titanoboa.system.auth/auth-system
                                                              :autostart  true}}
                             :jobs-repo-path "/mnt/efs/titanoboa/repo/"
                             :steps-repo-path "/mnt/efs/titanoboa/stepsrepo/"
                             :job-folder-path "/mnt/efs/titanoboa/jobs/"
                             :enable-cluster   false
                             :jetty {:ssl-port 443
                                     :join? false
                                     :ssl? true
                                     :http? false
                                     :keystore "/mnt/efs/titanoboa/keystore.jks"
                                     :key-password  "YourSslKeystorePassword"}
                             :archive-ds-ks [:archival-system :system :db-pool]
                             :auth? true
                             :auth-ds-ks [:auth-system :system :db-pool]
                             :auth-conf {:privkey "/mnt/efs/titanoboa/auth_privkey.pem"
                                         :passphrase "YourJwtPrivateKeyPassphrase"
                                         :pubkey  "/mnt/efs/titanoboa/auth_pubkey.pem"}
                             :systems-config {:core
                                              {:new-jobs-chan (clojure.core.async/chan (clojure.core.async/dropping-buffer 1024))
                                               :jobs-chan (clojure.core.async/chan (clojure.core.async/dropping-buffer 1024))
                                               :finished-jobs-chan archival-queue-local}
                                              :archival-system (merge {:finished-jobs-chan archival-queue-local}
                                                                      (edn/read-string (slurp "/mnt/efs/titanoboa/db-config.edn")))
                                              :auth-system (edn/read-string (slurp "/mnt/efs/titanoboa/db-config.edn"))}}))

