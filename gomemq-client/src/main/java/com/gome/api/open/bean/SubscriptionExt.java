package com.gome.api.open.bean;

/**
 * @author tantexian
 * @since 2016/6/27
 */
public class SubscriptionExt extends Subscription {
    private boolean persistence = true;

    public SubscriptionExt() {
    }

    public boolean isPersistence() {
        return this.persistence;
    }

    public void setPersistence(boolean persistence) {
        this.persistence = persistence;
    }

    public int hashCode() {
        return super.hashCode();
    }

    public boolean equals(Object obj) {
        return super.equals(obj);
    }

    public String toString() {
        return "Subscription [topic=" + super.getTopic() + ", expression=" + super.getExpression() + ", persistence=" + this.persistence + "]";
    }
}
