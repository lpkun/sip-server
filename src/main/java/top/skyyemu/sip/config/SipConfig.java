package top.skyyemu.sip.config;

import gov.nist.javax.sip.SipProviderImpl;
import gov.nist.javax.sip.SipStackImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import top.skyyemu.sip.listeners.SipProcessorListener;

import javax.sip.*;
import javax.sip.address.AddressFactory;
import javax.sip.header.HeaderFactory;
import javax.sip.message.MessageFactory;
import java.util.Properties;
import java.util.TooManyListenersException;

/**
 * @Description SIP后续用到的Bean
 * @Author LPkun
 * @Date 2024/4/30 16:49
 */
@Configuration
@Slf4j
@EnableConfigurationProperties(value = SipProperties.class)
public class SipConfig {

    private SipStackImpl sipStack;
    private SipFactory sipFactory;

    /**
     * 配置sip工厂bean
     */
    @Bean("sipFactory")
    SipFactory createSipFactory() {
        sipFactory = SipFactory.getInstance();
        sipFactory.setPathName("gov.nist");
        return sipFactory;
    }

    /**
     * 配置sip消息工厂bean
     */
    @Bean("messageFactory")
    MessageFactory createMessageFactory(SipFactory sipFactory) throws PeerUnavailableException {
        return sipFactory.createMessageFactory();
    }

    /**
     * 配置sip请求头工厂bean
     */
    @Bean("headerFactory")
    HeaderFactory createHeaderFactory(SipFactory sipFactory) throws PeerUnavailableException {
        return sipFactory.createHeaderFactory();
    }

    /**
     * 配置sip地址工厂bean
     */
    @Bean("addressFactory")
    AddressFactory createAddressFactory(SipFactory sipFactory) throws PeerUnavailableException {
        return sipFactory.createAddressFactory();
    }

    /**
     * sip堆栈
     */
    @Bean("sipStack")
    SipStack createSipStack() throws PeerUnavailableException {
        Properties properties = new Properties();
        properties.setProperty("javax.sip.STACK_NAME", "sipphone");
        properties.setProperty("gov.nist.javax.sip.TRACE_LEVEL", "0");
        properties.setProperty("gov.nist.javax.sip.DEBUG_LOG", "javasipwebdebug.log");
        properties.setProperty("gov.nist.javax.sip.SERVER_LOG", "javasipwebinfo.log");
        sipStack = (SipStackImpl) sipFactory.createSipStack(properties);
        return sipStack;
    }

    /**
     * tcpSipProvider监听
     */
    @Bean(name = "tcpSipProvider")
    SipProviderImpl startTcpListener(SipProperties sipProperties, SipProcessorListener sipProcessorListener) throws ObjectInUseException, TooManyListenersException, TransportNotSupportedException, InvalidArgumentException {
        ListeningPoint tcpListeningPoint = sipStack.createListeningPoint(sipProperties.getMonitorIp(), sipProperties.getPort(), "TCP");
        SipProviderImpl tcpSipProvider = (SipProviderImpl) sipStack.createSipProvider(tcpListeningPoint);
        tcpSipProvider.setDialogErrorsAutomaticallyHandled();
        tcpSipProvider.addSipListener(sipProcessorListener);
        log.info("增加TCP监听点成功，监听IP：{}，端口：{}", sipProperties.getMonitorIp(), sipProperties.getPort());
        return tcpSipProvider;
    }

    /**
     * udpSipProvider监听
     */
    @Bean(name = "udpSipProvider")
    SipProviderImpl startUdpListener(SipProperties sipProperties, SipProcessorListener sipProcessorListener) throws TransportNotSupportedException, InvalidArgumentException, ObjectInUseException, TooManyListenersException {
        ListeningPoint udpListeningPoint = sipStack.createListeningPoint(sipProperties.getMonitorIp(), sipProperties.getPort(), "UDP");
        SipProviderImpl udpSipProvider = (SipProviderImpl) sipStack.createSipProvider(udpListeningPoint);
        udpSipProvider.addSipListener(sipProcessorListener);
        log.info("增加UDP监听点成功，监听IP：{}，端口：{}", sipProperties.getMonitorIp(), sipProperties.getPort());
        return udpSipProvider;
    }
}
