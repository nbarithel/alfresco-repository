/*
 * Copyright (C) 2005-2007 Alfresco Software Limited.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.

 * As a special exception to the terms and conditions of version 2.0 of 
 * the GPL, you may redistribute this Program in connection with Free/Libre 
 * and Open Source Software ("FLOSS") applications as described in Alfresco's 
 * FLOSS exception.  You should have recieved a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing"
 */
package org.alfresco.repo.tagging;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.action.ParameterDefinitionImpl;
import org.alfresco.repo.action.executer.ActionExecuterAbstractBase;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ParameterDefinition;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.tagging.TagDetails;
import org.alfresco.service.cmr.tagging.TagScope;
import org.alfresco.service.cmr.tagging.TaggingService;

/**
 * Update tag scopes action executer.
 * 
 * NOTE:  This action is used to facilitate the async update of tag scopes.  It is not intended for genereral useage.
 * 
 * @author Roy Wetherall
 */
public class UpdateTagScopesActionExecuter extends ActionExecuterAbstractBase
{
    /** Node Service */
    private NodeService nodeService;
    
    /** Content Service */
    private ContentService contentService;
    
    /** Tagging Service */
    private TaggingService taggingService;
    
    /** Action name and parameters */
    public static final String NAME = "update-tagscope";
    public static final String PARAM_TAG_NAME = "tag_name";
    public static final String PARAM_ADD_TAG = "add_tag";   
    
    /**
     * Set the node service
     * 
     * @param nodeService   node service
     */
    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }
    
    /**
     * Set the content service
     * 
     * @param contentService    the content service
     */
    public void setContentService(ContentService contentService)
    {
        this.contentService = contentService;
    }
    
    /**
     * Set the tagging service
     * 
     * @param taggingService    the tagging service
     */
    public void setTaggingService(TaggingService taggingService)
    {
        this.taggingService = taggingService;
    }
    
    /**
     * @see org.alfresco.repo.action.executer.ActionExecuterAbstractBase#executeImpl(org.alfresco.service.cmr.action.Action, org.alfresco.service.cmr.repository.NodeRef)
     */
    @Override
    protected void executeImpl(Action action, NodeRef actionedUponNodeRef)
    {
        if (this.nodeService.exists(actionedUponNodeRef) == true)
        {
            // Get the parameter values
            String tagName = (String)action.getParameterValue(PARAM_TAG_NAME);
            Boolean isAdd = (Boolean)action.getParameterValue(PARAM_ADD_TAG);
            
            // Get the tag scopes for the actioned upon node
            List<TagScope> tagScopes = this.taggingService.findAllTagScopes(actionedUponNodeRef);
            
            // Update each tag scope
            for (TagScope tagScope : tagScopes)
            {
                NodeRef tagScopeNodeRef = tagScope.getNodeRef();                
                List<TagDetails> tags = null;
                
                // Get the current tags
                ContentReader contentReader = this.contentService.getReader(tagScopeNodeRef, ContentModel.PROP_TAGSCOPE_CACHE);
                if (contentReader == null)
                {
                    tags = new ArrayList<TagDetails>(1);
                }
                else
                {
                    tags = TaggingServiceImpl.readTagDetails(contentReader.getContentInputStream());
                }
                    
                TagDetails currentTag = null;
                for (TagDetails tag : tags)
                {
                    if (tag.getTagName().equals(tagName) == true)
                    {
                        currentTag = tag;
                        break;
                    }
                }
                
                if (isAdd == true)
                {
                    if (currentTag == null)
                    {
                        tags.add(new TagDetailsImpl(tagName, 1));
                    }
                    else
                    {
                        ((TagDetailsImpl)currentTag).incrementCount();
                    }
                 
                }
                else
                {
                    if (currentTag != null)
                    {
                        int currentTagCount = currentTag.getTagCount();                        
                        if (currentTagCount == 1)
                        {
                            tags.remove(currentTag);
                        }
                        else
                        {
                            ((TagDetailsImpl)currentTag).decrementCount();
                        }
                    }
                }
                
                // Order the list
                Collections.sort(tags);
                
                // Write new content back to tag scope
                String tagContent = TaggingServiceImpl.tagDetailsToString(tags);
                ContentWriter contentWriter = this.contentService.getWriter(tagScopeNodeRef, ContentModel.PROP_TAGSCOPE_CACHE, true);
                contentWriter.setEncoding("UTF-8");
                contentWriter.setMimetype(MimetypeMap.MIMETYPE_TEXT_PLAIN);
                contentWriter.putContent(tagContent);
                
            }
        }
    }

    /**
     * @see org.alfresco.repo.action.ParameterizedItemAbstractBase#addParameterDefinitions(java.util.List)
     */
    @Override
    protected void addParameterDefinitions(List<ParameterDefinition> paramList)
    {
        paramList.add(new ParameterDefinitionImpl(PARAM_TAG_NAME, DataTypeDefinition.TEXT, true, getParamDisplayLabel(PARAM_TAG_NAME)));
        paramList.add(new ParameterDefinitionImpl(PARAM_ADD_TAG, DataTypeDefinition.BOOLEAN, false, getParamDisplayLabel(PARAM_ADD_TAG)));
    }

}
