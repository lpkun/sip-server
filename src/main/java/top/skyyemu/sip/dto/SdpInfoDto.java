package top.skyyemu.sip.dto;

import lombok.Data;
import top.skyyemu.sip.enums.VoiceTypeEnum;

/**
 * @Description 解析SDP后
 * @Author LPkun
 * @Date 2024/5/19 15:38
 */
@Data
public class SdpInfoDto {
    /**
     * ip
     */
    private String ip;
    /**
     * 端口
     */
    private Integer port;
    /**
     * 有效的负载，解析出来一种就够了一般多是：PCMA || PCMU
     */
    private VoiceTypeEnum voiceType;
    /**
     * 采样率
     */
    private Integer samplingRate;
}
