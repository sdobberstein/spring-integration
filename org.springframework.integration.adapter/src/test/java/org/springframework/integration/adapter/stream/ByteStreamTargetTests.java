/*
 * Copyright 2002-2008 the original author or authors.
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

package org.springframework.integration.adapter.stream;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.junit.Before;
import org.junit.Test;

import org.springframework.integration.bus.PollingDispatcher;
import org.springframework.integration.channel.QueueChannel;
import org.springframework.integration.dispatcher.BroadcastingDispatcher;
import org.springframework.integration.message.GenericMessage;
import org.springframework.integration.message.StringMessage;
import org.springframework.integration.scheduling.PollingSchedule;

/**
 * @author Mark Fisher
 */
public class ByteStreamTargetTests {

	private QueueChannel channel;

	private PollingDispatcher dispatcher;


	@Before
	public void initialize() {
		this.channel = new QueueChannel(10);
		this.dispatcher = new PollingDispatcher(channel, new PollingSchedule(0), new BroadcastingDispatcher());
	}


	@Test
	public void testSingleByteArray() {
		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		ByteStreamTarget target = new ByteStreamTarget(stream);
		target.send(new GenericMessage<byte[]>(new byte[] {1,2,3}));
		byte[] result = stream.toByteArray();
		assertEquals(3, result.length);
		assertEquals(1, result[0]);
		assertEquals(2, result[1]);
		assertEquals(3, result[2]);
	}

	@Test
	public void testSingleString() {
		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		ByteStreamTarget target = new ByteStreamTarget(stream);
		target.send(new StringMessage("foo"));
		byte[] result = stream.toByteArray();
		assertEquals(3, result.length);
		assertEquals("foo", new String(result));
	}

	@Test
	public void testMaxMessagesPerTaskSameAsMessageCount() {
		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		ByteStreamTarget target = new ByteStreamTarget(stream);
		dispatcher.setMaxMessagesPerPoll(3);
		dispatcher.subscribe(target);
		channel.send(new GenericMessage<byte[]>(new byte[] {1,2,3}), 0);
		channel.send(new GenericMessage<byte[]>(new byte[] {4,5,6}), 0);
		channel.send(new GenericMessage<byte[]>(new byte[] {7,8,9}), 0);
		dispatcher.run();
		byte[] result = stream.toByteArray();
		assertEquals(9, result.length);
		assertEquals(1, result[0]);
		assertEquals(9, result[8]);
	}

	@Test
	public void testMaxMessagesPerTaskLessThanMessageCount() {
		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		ByteStreamTarget target = new ByteStreamTarget(stream);
		dispatcher.setMaxMessagesPerPoll(2);
		dispatcher.subscribe(target);
		channel.send(new GenericMessage<byte[]>(new byte[] {1,2,3}), 0);
		channel.send(new GenericMessage<byte[]>(new byte[] {4,5,6}), 0);
		channel.send(new GenericMessage<byte[]>(new byte[] {7,8,9}), 0);
		dispatcher.run();
		byte[] result = stream.toByteArray();
		assertEquals(6, result.length);
		assertEquals(1, result[0]);
	}

	@Test
	public void testMaxMessagesPerTaskExceedsMessageCount() {
		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		ByteStreamTarget target = new ByteStreamTarget(stream);
		dispatcher.setMaxMessagesPerPoll(5);
		dispatcher.setReceiveTimeout(0);
		dispatcher.subscribe(target);
		channel.send(new GenericMessage<byte[]>(new byte[] {1,2,3}), 0);
		channel.send(new GenericMessage<byte[]>(new byte[] {4,5,6}), 0);
		channel.send(new GenericMessage<byte[]>(new byte[] {7,8,9}), 0);
		dispatcher.run();
		byte[] result = stream.toByteArray();
		assertEquals(9, result.length);
		assertEquals(1, result[0]);
	}

	@Test
	public void testMaxMessagesLessThanMessageCountWithMultipleDispatches() {
		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		ByteStreamTarget target = new ByteStreamTarget(stream);
		dispatcher.setMaxMessagesPerPoll(2);
		dispatcher.setReceiveTimeout(0);
		dispatcher.subscribe(target);
		channel.send(new GenericMessage<byte[]>(new byte[] {1,2,3}), 0);
		channel.send(new GenericMessage<byte[]>(new byte[] {4,5,6}), 0);
		channel.send(new GenericMessage<byte[]>(new byte[] {7,8,9}), 0);
		dispatcher.run();
		byte[] result1 = stream.toByteArray();
		assertEquals(6, result1.length);
		assertEquals(1, result1[0]);
		dispatcher.run();
		byte[] result2 = stream.toByteArray();
		assertEquals(9, result2.length);
		assertEquals(1, result2[0]);
		assertEquals(7, result2[6]);
	}

	@Test
	public void testMaxMessagesExceedsMessageCountWithMultipleDispatches() {
		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		ByteStreamTarget target = new ByteStreamTarget(stream);
		dispatcher.setMaxMessagesPerPoll(5);
		dispatcher.setReceiveTimeout(0);
		dispatcher.subscribe(target);
		channel.send(new GenericMessage<byte[]>(new byte[] {1,2,3}), 0);
		channel.send(new GenericMessage<byte[]>(new byte[] {4,5,6}), 0);
		channel.send(new GenericMessage<byte[]>(new byte[] {7,8,9}), 0);
		dispatcher.run();
		byte[] result1 = stream.toByteArray();
		assertEquals(9, result1.length);
		assertEquals(1, result1[0]);
		dispatcher.run();
		byte[] result2 = stream.toByteArray();
		assertEquals(9, result2.length);	
		assertEquals(1, result2[0]);
	}

	@Test
	public void testStreamResetBetweenDispatches() {
		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		ByteStreamTarget target = new ByteStreamTarget(stream);
		dispatcher.setMaxMessagesPerPoll(2);
		dispatcher.setReceiveTimeout(0);
		dispatcher.subscribe(target);
		channel.send(new GenericMessage<byte[]>(new byte[] {1,2,3}), 0);
		channel.send(new GenericMessage<byte[]>(new byte[] {4,5,6}), 0);
		channel.send(new GenericMessage<byte[]>(new byte[] {7,8,9}), 0);
		dispatcher.run();
		byte[] result1 = stream.toByteArray();
		assertEquals(6, result1.length);
		stream.reset();
		dispatcher.run();
		byte[] result2 = stream.toByteArray();
		assertEquals(3, result2.length);
		assertEquals(7, result2[0]);
	}

	@Test
	public void testStreamWriteBetweenDispatches() throws IOException {
		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		ByteStreamTarget target = new ByteStreamTarget(stream);
		dispatcher.setMaxMessagesPerPoll(2);
		dispatcher.setReceiveTimeout(0);
		dispatcher.subscribe(target);
		channel.send(new GenericMessage<byte[]>(new byte[] {1,2,3}), 0);
		channel.send(new GenericMessage<byte[]>(new byte[] {4,5,6}), 0);
		channel.send(new GenericMessage<byte[]>(new byte[] {7,8,9}), 0);
		dispatcher.run();
		byte[] result1 = stream.toByteArray();
		assertEquals(6, result1.length);
		stream.write(new byte[] {123});
		stream.flush();
		dispatcher.run();
		byte[] result2 = stream.toByteArray();
		assertEquals(10, result2.length);
		assertEquals(1, result2[0]);
		assertEquals(123, result2[6]);
		assertEquals(7, result2[7]);
	}

}
