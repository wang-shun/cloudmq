package com.cloudzone.cloudmq.api.open.bean;

/**
 * @author tantexian
 * @since 2016/6/27
 */
public class Subscription {
    private String topic;
    private String expression;

    public Subscription() {
    }

    public String getTopic() {
        return this.topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public String getExpression() {
        return this.expression;
    }

    public void setExpression(String expression) {
        this.expression = expression;
    }

    public int hashCode() {
        boolean prime = true;
        byte result = 1;
        int result1 = 31 * result + (this.topic == null?0:this.topic.hashCode());
        return result1;
    }

    public boolean equals(Object obj) {
        if(this == obj) {
            return true;
        } else if(obj == null) {
            return false;
        } else if(this.getClass() != obj.getClass()) {
            return false;
        } else {
            Subscription other = (Subscription)obj;
            if(this.topic == null) {
                if(other.topic != null) {
                    return false;
                }
            } else if(!this.topic.equals(other.topic)) {
                return false;
            }

            return true;
        }
    }

    public String toString() {
        return "Subscription [topic=" + this.topic + ", expression=" + this.expression + "]";
    }
}
