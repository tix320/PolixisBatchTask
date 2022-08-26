package com.polixis.batchtask.job;

import java.io.IOException;
import java.io.InputStream;
import java.util.function.Function;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import com.polixis.batchtask.util.NonClosableInputStream;
import org.springframework.batch.item.*;
import org.springframework.core.io.Resource;

public class ZipItemReader<T> implements ItemStreamReader<T> {

	private final Resource resource;

	private final Function<InputStream, ItemStreamReader<T>> entryReaderFactory;

	private ExecutionContext executionContext;

	private ZipInputStream zipInputStream;

	private ItemStreamReader<T> currentEntryReader;

	public ZipItemReader(Resource resource, Function<InputStream, ItemStreamReader<T>> entryReaderFactory) {
		this.resource = resource;

		this.entryReaderFactory = entryReaderFactory;
	}

	@Override
	public void open(ExecutionContext executionContext) throws ItemStreamException {
		this.executionContext = executionContext;
		try {
			this.zipInputStream = new ZipInputStream(resource.getInputStream());
			jumpToNextEntry();
		} catch (IOException e) {
			throw new ItemStreamException("Failed to open zip resource", e);
		}
	}

	@Override
	public void update(ExecutionContext executionContext) throws ItemStreamException {

	}

	@Override
	public void close() throws ItemStreamException {
		currentEntryReader = null;
		try {
			this.zipInputStream.close();
		} catch (IOException e) {
			throw new ItemStreamException("Failed to close zip resource", e);
		}
	}

	@Override
	public T read() throws Exception {
		T data = currentEntryReader.read();
		if (data == null) {
			if (!jumpToNextEntry()) {
				return null;
			}

			data = currentEntryReader.read();
		}

		return data;
	}

	private boolean jumpToNextEntry() throws IOException {
		if (currentEntryReader != null) {
			currentEntryReader.close();
		}

		ZipEntry nextEntry = zipInputStream.getNextEntry();
		currentEntryReader = entryReaderFactory.apply(new NonClosableInputStream(zipInputStream));
		currentEntryReader.open(executionContext);
		return nextEntry != null;
	}
}
