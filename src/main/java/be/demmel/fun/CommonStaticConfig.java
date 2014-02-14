package be.demmel.fun;

import java.util.HashMap;
import java.util.Map;

import be.demmel.protocol.ucp.UCPOperationType;
import be.demmel.protocol.ucp.serialization.O_01_CallInputDeserializer;
import be.demmel.protocol.ucp.serialization.O_01_CallInputSerializer;
import be.demmel.protocol.ucp.serialization.O_02_MultipleAddressCallInputDeserializer;
import be.demmel.protocol.ucp.serialization.O_02_MultipleAddressCallInputSerializer;
import be.demmel.protocol.ucp.serialization.O_03_CallInputWithMultipleSupplementaryServicesDeserializer;
import be.demmel.protocol.ucp.serialization.O_03_CallInputWithMultipleSupplementaryServicesSerializer;
import be.demmel.protocol.ucp.serialization.O_30_SmsMessageTransferDeserializer;
import be.demmel.protocol.ucp.serialization.O_30_SmsMessageTransferSerializer;
import be.demmel.protocol.ucp.serialization.O_31_AlertDeserializer;
import be.demmel.protocol.ucp.serialization.O_31_AlertSerializer;
import be.demmel.protocol.ucp.serialization.O_5x_AbstractDataTypeDeserializer;
import be.demmel.protocol.ucp.serialization.O_5x_AbstractDataTypeSerializer;
import be.demmel.protocol.ucp.serialization.O_6x_AbstractDataTypeDeserializer;
import be.demmel.protocol.ucp.serialization.O_6x_AbstractDataTypeSerializer;
import be.demmel.protocol.ucp.serialization.UCPOperationDeserializer;
import be.demmel.protocol.ucp.serialization.UCPOperationSerializer;
import be.demmel.protocol.ucp.serialization.UCPPacketDeserializerImpl;
import be.demmel.protocol.ucp.serialization.UCPPacketSerializerImpl;

public class CommonStaticConfig {
	public static final UCPPacketSerializerImpl PACKET_SERIALIZER;
	public static final UCPPacketDeserializerImpl PACKET_DESERIALIZER;

	static {
		O_31_AlertSerializer o31Serializer = new O_31_AlertSerializer();
		O_6x_AbstractDataTypeSerializer o6xSerializer = new O_6x_AbstractDataTypeSerializer();
		O_5x_AbstractDataTypeSerializer o5xSerializer = new O_5x_AbstractDataTypeSerializer();
		O_01_CallInputSerializer o01Serializer = new O_01_CallInputSerializer();
		O_02_MultipleAddressCallInputSerializer o02Serializer = new O_02_MultipleAddressCallInputSerializer();
		O_03_CallInputWithMultipleSupplementaryServicesSerializer o03Serializer = new O_03_CallInputWithMultipleSupplementaryServicesSerializer();
		O_30_SmsMessageTransferSerializer o30Serializer = new O_30_SmsMessageTransferSerializer();
		Map<UCPOperationType, UCPOperationSerializer> operationSerializers = new HashMap<UCPOperationType, UCPOperationSerializer>();
		operationSerializers.put(UCPOperationType.ALERT, o31Serializer);
		operationSerializers.put(UCPOperationType.SESSION_MANAGEMENT, o6xSerializer);
		operationSerializers.put(UCPOperationType.LIST_MANAGEMENT, o6xSerializer);
		operationSerializers.put(UCPOperationType.SUBMIT_SHORT_MESSAGE, o5xSerializer);
		operationSerializers.put(UCPOperationType.MODIFY_MESSAGE, o5xSerializer);
		operationSerializers.put(UCPOperationType.INQUIRY_MESSAGE, o5xSerializer);
		operationSerializers.put(UCPOperationType.DELETE_MESSAGE, o5xSerializer);
		operationSerializers.put(UCPOperationType.DELIVER_SHORT_MESSAGE, o5xSerializer);
		operationSerializers.put(UCPOperationType.DELIVER_NOTIFICATION, o5xSerializer);
		operationSerializers.put(UCPOperationType.RESPONSE_INQUIRY_MESSAGE, o5xSerializer);
		operationSerializers.put(UCPOperationType.RESPONSE_DELETE_MESSAGE, o5xSerializer);
		operationSerializers.put(UCPOperationType.CALL_INPUT, o01Serializer);
		operationSerializers.put(UCPOperationType.MULTIPLE_ADDRESS_CALL_INPUT, o02Serializer);
		operationSerializers.put(UCPOperationType.CALL_INPUT_WITH_MULTIPLE_SUPPLEMENTARY_SERVICES, o03Serializer);
		operationSerializers.put(UCPOperationType.SMS_MESSAGE_TRANSFER, o30Serializer);

		PACKET_SERIALIZER = new UCPPacketSerializerImpl(operationSerializers);

		O_31_AlertDeserializer o31Deserializer = new O_31_AlertDeserializer();
		O_5x_AbstractDataTypeDeserializer o5xDeserializer = new O_5x_AbstractDataTypeDeserializer();
		O_6x_AbstractDataTypeDeserializer o6xDeserializer = new O_6x_AbstractDataTypeDeserializer();
		O_01_CallInputDeserializer o01Deserializer = new O_01_CallInputDeserializer();
		O_02_MultipleAddressCallInputDeserializer o02Deserializer = new O_02_MultipleAddressCallInputDeserializer();
		O_03_CallInputWithMultipleSupplementaryServicesDeserializer o03Deserializer = new O_03_CallInputWithMultipleSupplementaryServicesDeserializer();
		O_30_SmsMessageTransferDeserializer o30Deserializer = new O_30_SmsMessageTransferDeserializer();
		Map<UCPOperationType, UCPOperationDeserializer> operationDeserializers = new HashMap<UCPOperationType, UCPOperationDeserializer>();
		operationDeserializers.put(UCPOperationType.ALERT, o31Deserializer);
		operationDeserializers.put(UCPOperationType.SESSION_MANAGEMENT, o6xDeserializer);
		operationDeserializers.put(UCPOperationType.LIST_MANAGEMENT, o6xDeserializer);
		operationDeserializers.put(UCPOperationType.SUBMIT_SHORT_MESSAGE, o5xDeserializer);
		operationDeserializers.put(UCPOperationType.MODIFY_MESSAGE, o5xDeserializer);
		operationDeserializers.put(UCPOperationType.INQUIRY_MESSAGE, o5xDeserializer);
		operationDeserializers.put(UCPOperationType.DELETE_MESSAGE, o5xDeserializer);
		operationDeserializers.put(UCPOperationType.DELIVER_SHORT_MESSAGE, o5xDeserializer);
		operationDeserializers.put(UCPOperationType.DELIVER_NOTIFICATION, o5xDeserializer);
		operationDeserializers.put(UCPOperationType.RESPONSE_INQUIRY_MESSAGE, o5xDeserializer);
		operationDeserializers.put(UCPOperationType.RESPONSE_DELETE_MESSAGE, o5xDeserializer);
		operationDeserializers.put(UCPOperationType.CALL_INPUT, o01Deserializer);
		operationDeserializers.put(UCPOperationType.MULTIPLE_ADDRESS_CALL_INPUT, o02Deserializer);
		operationDeserializers.put(UCPOperationType.CALL_INPUT_WITH_MULTIPLE_SUPPLEMENTARY_SERVICES, o03Deserializer);
		operationDeserializers.put(UCPOperationType.SMS_MESSAGE_TRANSFER, o30Deserializer);

		PACKET_DESERIALIZER = new UCPPacketDeserializerImpl(operationDeserializers);
	}
}
