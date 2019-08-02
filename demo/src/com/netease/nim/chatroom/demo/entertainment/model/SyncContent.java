package com.netease.nim.chatroom.demo.entertainment.model;

import com.alibaba.fastjson.JSON;

import java.util.List;

//推流的内容
public class SyncContent {

    /**
     * MixAudioInfo : {"NodesInfo":[{"Energy":726,"Uid":"6321409930"}],"SelectMember":1,"TotalMember":1}
     * Type : 0
     * Verson : 0
     */

    private String MixAudioInfo;
    private int Type;
    private int Verson;

    public String getMixAudioInfo() {
        return MixAudioInfo;
    }

    public void setMixAudioInfo(String MixAudioInfo) {
        this.MixAudioInfo = MixAudioInfo;
    }

    public int getType() {
        return Type;
    }

    public void setType(int Type) {
        this.Type = Type;
    }

    public int getVerson() {
        return Verson;
    }

    public void setVerson(int Verson) {
        this.Verson = Verson;
    }


    public List<NodesInfo.NodesInfoBean> getNodesInfoBeanByJson(String json) {
        SyncContent syncContent = JSON.parseObject(json, SyncContent.class);
        String tempJson = syncContent.getMixAudioInfo();
        NodesInfo nodesInfo = JSON.parseObject(tempJson, NodesInfo.class);
        return nodesInfo.getNodesInfo();
    }

    public static class NodesInfo {

        /**
         * NodesInfo : [{"Energy":726,"Uid":"6321409930"}]
         * SelectMember : 1
         * TotalMember : 1
         */

        private int SelectMember;
        private int TotalMember;
        private List<NodesInfoBean> NodesInfo;

        public int getSelectMember() {
            return SelectMember;
        }

        public void setSelectMember(int SelectMember) {
            this.SelectMember = SelectMember;
        }

        public int getTotalMember() {
            return TotalMember;
        }

        public void setTotalMember(int TotalMember) {
            this.TotalMember = TotalMember;
        }

        public List<NodesInfoBean> getNodesInfo() {
            return NodesInfo;
        }

        public void setNodesInfo(List<NodesInfoBean> NodesInfo) {
            this.NodesInfo = NodesInfo;
        }

        public static class NodesInfoBean {
            /**
             * Energy : 726
             * Uid : 6321409930
             */

            private int Energy;
            private String Uid;

            public int getEnergy() {
                return Energy;
            }

            public void setEnergy(int Energy) {
                this.Energy = Energy;
            }

            public String getUid() {
                return Uid;
            }

            public void setUid(String Uid) {
                this.Uid = Uid;
            }
        }
    }

}
