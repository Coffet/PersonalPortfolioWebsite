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

        public void setUploadRoot(String uploadRoot) {
            this.uploadRoot = uploadRoot;
        }

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

            public boolean isEnabled() {
                return enabled;
            }

            public void setEnabled(boolean enabled) {
                this.enabled = enabled;
            }

            public String getEndpoint() {
                return endpoint;
            }

            public void setEndpoint(String endpoint) {
                this.endpoint = endpoint;
            }

            public String getAccessKey() {
                return accessKey;
            }

            public void setAccessKey(String accessKey) {
                this.accessKey = accessKey;
            }

            public String getSecretKey() {
                return secretKey;
            }

            public void setSecretKey(String secretKey) {
                this.secretKey = secretKey;
            }

            public String getBucket() {
                return bucket;
            }

            public void setBucket(String bucket) {
                this.bucket = bucket;
            }

            public String getRegion() {
                return region;
            }

            public void setRegion(String region) {
                this.region = region;
            }

            public String getTrustCert() {
                return trustCert;
            }

            public void setTrustCert(String trustCert) {
                this.trustCert = trustCert;
            }

            public boolean isMigrate() {
                return migrate;
            }

            public void setMigrate(boolean migrate) {
                this.migrate = migrate;
            }

            public boolean isDeleteLocalAfterVerify() {
                return deleteLocalAfterVerify;
            }

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
