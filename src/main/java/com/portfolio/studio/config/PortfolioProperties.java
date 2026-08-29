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

        public String getUploadRoot() {
            return uploadRoot;
        }

        public void setUploadRoot(String uploadRoot) {
            this.uploadRoot = uploadRoot;
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
