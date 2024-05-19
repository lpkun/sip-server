package top.skyyemu.sip.enums;

/**
 * @Description 公共语音传输字节的数据类型
 * @Author LPkun
 * @Date 2023/12/19 10:12
 */
public enum VoiceTypeEnum {

    /**
     * PCM 使用alaw压缩算法
     */
    PCMA("PCMA", ".pcm"),
    /**
     * PCM 使用ulaw压缩算法
     */
    PCMU("PCMU", ".pcm");

    /**
     * 传输数据类型名称
     */
    private String voiceTypeName;
    /**
     * 合并数据的后缀
     */
    private String suffix;

    VoiceTypeEnum(String voiceTypeName, String suffix) {
        this.voiceTypeName = voiceTypeName;
        this.suffix = suffix;
    }

    public String getVoiceTypeName() {
        return voiceTypeName;
    }

    public String getSuffix() {
        return suffix;
    }
}
