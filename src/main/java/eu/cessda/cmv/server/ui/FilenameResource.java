package eu.cessda.cmv.server.ui;

/*-
 * #%L
 * CESSDA Metadata Validator
 * %%
 * Copyright (C) 2020 - 2026 CESSDA ERIC
 * %%
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
 * #L%
 */

import org.springframework.core.io.ByteArrayResource;
import org.springframework.lang.NonNull;

import java.util.Objects;

public class FilenameResource extends ByteArrayResource
{
	private final String fileName;

	public FilenameResource( byte[] byteArray, String fileName )
	{
		super( byteArray );
		this.fileName = fileName;
	}

	@Override
	public String getFilename()
	{
		return fileName;
	}

	@Override
	@NonNull
	public String getDescription()
	{
		return "File name resource [" + this.fileName + "]";
	}

	@Override
	public boolean equals( Object o )
	{
		if ( this == o ) return true;
		if ( !( o instanceof FilenameResource that ) ) return false;
		if ( !super.equals( o ) ) return false;
		return Objects.equals( fileName, that.fileName );
	}

	@Override
	public int hashCode()
	{
		return Objects.hash( super.hashCode(), fileName );
	}
}
