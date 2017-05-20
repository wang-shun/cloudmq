package com.alibaba.rocketmq.domain.gmq;

import com.alibaba.rocketmq.tools.admin.api.MessageTrack;

/**
 * @author: tianyuliang
 * @since: 2017/3/3
 */
public class MessageTrackExt extends MessageTrack {

    private int trackCode;

    private String trackDescription;

    public String getTrackDescription() {
        return trackDescription;
    }

    public void setTrackDescription(String trackDescription) {
        this.trackDescription = trackDescription;
    }
    public int getTrackCode() {
        return trackCode;
    }

    public void setTrackCode(int trackCode) {
        this.trackCode = trackCode;
    }


}
