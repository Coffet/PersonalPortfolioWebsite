package com.portfolio.studio.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "portfolio")
public class PortfolioProperties {

    private final Storage storage = new Storage();
    private final Seed seed = new Seed();
    private final Security security = new Security();

    public Storage getStorage() {
        return storage;
    }

    public Seed getSeed() {
        return seed;
    }

    public Security getSecurity() {
        return security;
    }

    public static class Storage {
        private String uploadRoot = "storage/uploads";
        private final S3 s3 = new S3();

        public String getUploadRoot() {
            return uploadRoot;
        }

        /**
         * Sets the root directory used for uploaded files.
         *
         * @param uploadRoot the upload root directory
         */
        public void setUploadRoot(String uploadRoot) {
            this.uploadRoot = uploadRoot;
        }

        /**
         * Provides the S3 storage configuration.
         *
         * @return the S3 storage configuration
         */
        public S3 getS3() {
            return s3;
        }

        public static class S3 {
            private boolean enabled;
            private String endpoint = "";
            private String accessKey = "";
            private String secretKey = "";
            private String bucket = "";
            private String region = "us-east-1";
            private String trustCert = "";
            private boolean migrate;
            private boolean deleteLocalAfterVerify;

            /**
             * Determines whether S3 storage is enabled.
             *
             * @return {@code true} if S3 storage is enabled, {@code false} otherwise
             */
            public boolean isEnabled() {
                return enabled;
            }

            /**
             * Configures whether S3 storage is enabled.
             *
             * @param enabled whether S3 storage is enabled
             */
            public void setEnabled(boolean enabled) {
                this.enabled = enabled;
            }

            /**
             * Retrieves the configured S3 endpoint.
             *
             * @return the configured S3 endpoint
             */
            public String getEndpoint() {
                return endpoint;
            }

            /**
             * Sets the S3 service endpoint.
             *
             * @param endpoint the S3 service endpoint
             */
            public void setEndpoint(String endpoint) {
                this.endpoint = endpoint;
            }

            /**
             * Gets the access key used for S3 access.
             *
             * @return the configured S3 access key
             */
            public String getAccessKey() {
                return accessKey;
            }

            public void setAccessKey(String accessKey) {
                this.accessKey = accessKey;
            }

            /**
             * Gets the S3 secret access key.
             *
             * @return the configured secret access key
             */
            public String getSecretKey() {
                return secretKey;
            }

            public void setSecretKey(String secretKey) {
                this.secretKey = secretKey;
            }

            /**
             * Gets the configured storage bucket name.
             *
             * @return the storage bucket name
             */
            public String getBucket() {
                return bucket;
            }

            /**
             * Sets the S3 bucket name.
             *
             * @param bucket the S3 bucket name
             */
            public void setBucket(String bucket) {
                this.bucket = bucket;
            }

            public String getRegion() {
                return region;
            }

            /**
             * Sets the S3 region.
             *
             * @param region the S3 region
             */
            public void setRegion(String region) {
                this.region = region;
            }

            /**
             * Retrieves the configured trust certificate.
             *
             * @return the trust certificate
             */
            public String getTrustCert() {
                return trustCert;
            }

            /**
             * Sets the certificate trust configuration.
             *
             * @param trustCert the certificate trust value
             */
            public void setTrustCert(String trustCert) {
                this.trustCert = trustCert;
            }

            /**
             * Determines whether storage migration is enabled.
             *
             * @return {@code true} if migration is enabled, {@code false} otherwise
             */
            public boolean isMigrate() {
                return migrate;
            }

            /**
             * Sets whether storage migration is enabled.
             *
             * @param migrate whether to enable storage migration
             */
            public void setMigrate(boolean migrate) {
                this.migrate = migrate;
            }

            /**
             * Determines whether local files are deleted after successful verification.
             *
             * @return {@code true} if local files are deleted after verification, {@code false} otherwise
             */
            public boolean isDeleteLocalAfterVerify() {
                return deleteLocalAfterVerify;
            }

            /**
             * Sets whether local files are deleted after successful verification.
             *
             * @param deleteLocalAfterVerify whether to delete verified local files
             */
            public void setDeleteLocalAfterVerify(boolean deleteLocalAfterVerify) {
                this.deleteLocalAfterVerify = deleteLocalAfterVerify;
            }
        }
    }

    public static class Seed {
        private final Owner owner = new Owner();

        public Owner getOwner() {
            return owner;
        }

        public static class Owner {
            private String username;
            private String password;

            public String getUsername() {
                return username;
            }

            public void setUsername(String username) {
                this.username = username;
            }

            public String getPassword() {
                return password;
            }

            public void setPassword(String password) {
                this.password = password;
            }
        }
    }

    public static class Security {
        private int loginLockThreshold = 5;
        private int loginLockMinutes = 15;

        public int getLoginLockThreshold() {
            return loginLockThreshold;
        }

        public void setLoginLockThreshold(int loginLockThreshold) {
            this.loginLockThreshold = loginLockThreshold;
        }

        public int getLoginLockMinutes() {
            return loginLockMinutes;
        }

        public void setLoginLockMinutes(int loginLockMinutes) {
            this.loginLockMinutes = loginLockMinutes;
        }
    }
}
