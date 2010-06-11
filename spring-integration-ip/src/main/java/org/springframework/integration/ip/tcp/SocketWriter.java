/*
 * Copyright 2002-2010 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.integration.ip.tcp;

import java.io.IOException;


/**
 * A general interface for writing to sockets.
 * 
 * @author Gary Russell
 *
 */
public interface SocketWriter {

	/**
	 * Write the entire buffer to the underlying socket. Appropriate wire
	 * protocols will be implemented so the receiving side can decode and
	 * reassemble the message, if packetized by the network.
	 * @param object The object to write.
	 * @throws IOException 
	 */
	void write(Object object) throws IOException;

	/**
	 * @param messageFormat the messageFormat to set
	 */
	public void setMessageFormat(int messageFormat);

}
