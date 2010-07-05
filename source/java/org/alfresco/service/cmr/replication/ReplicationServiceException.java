/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
 *
 * This file is part of Alfresco
 *
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 */
package org.alfresco.service.cmr.replication;

import org.alfresco.error.AlfrescoRuntimeException;

/**
 * Replication Service Exception Class
 * 
 * @author Nick Burch
 */
public class ReplicationServiceException extends AlfrescoRuntimeException
{
    private static final long serialVersionUID = 3961767729869569556L;

    /**
     * Constructs a Replication Service Exception with the specified message.
     * 
     * @param message 	the message string
     */
    public ReplicationServiceException(String message) 
    {
       super(message);
    }

    /**
     * Constructs a Replication Service Exception with the specified message and source exception.
     * 
     * @param message   the message string
     * @param source	the source exception
     */
	 public ReplicationServiceException(String message, Throwable source) 
    {
       super(message, source);
    }
}
