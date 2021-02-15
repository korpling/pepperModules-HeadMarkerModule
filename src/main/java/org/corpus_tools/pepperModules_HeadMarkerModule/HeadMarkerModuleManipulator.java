/**
 * Copyright 2016 GU.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 *
 */
package org.corpus_tools.pepperModules_HeadMarkerModule;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.corpus_tools.pepper.common.DOCUMENT_STATUS;
import org.corpus_tools.pepper.common.PepperConfiguration;
import org.corpus_tools.pepper.impl.PepperManipulatorImpl;
import org.corpus_tools.pepper.impl.PepperMapperImpl;
import org.corpus_tools.pepper.modules.PepperManipulator;
import org.corpus_tools.pepper.modules.PepperMapper;
import org.corpus_tools.pepper.modules.PepperModule;
import org.corpus_tools.pepper.modules.PepperModuleProperties;
import org.corpus_tools.pepper.modules.exceptions.PepperModuleDataException;
import org.corpus_tools.pepper.modules.exceptions.PepperModuleNotReadyException;
import org.corpus_tools.salt.SALT_TYPE;
import org.corpus_tools.salt.SaltFactory;
import org.corpus_tools.salt.common.SCorpus;
import org.corpus_tools.salt.common.SDocument;
import org.corpus_tools.salt.common.SDocumentGraph;
import org.corpus_tools.salt.common.SPointingRelation;
import org.corpus_tools.salt.common.SToken;
import org.corpus_tools.salt.common.STextualDS;
import org.corpus_tools.salt.core.GraphTraverseHandler;
import org.corpus_tools.salt.core.SAnnotation;
import org.corpus_tools.salt.core.SGraph.GRAPH_TRAVERSE_TYPE;
import org.corpus_tools.salt.core.SNode;
import org.corpus_tools.salt.common.SSpan;
import org.corpus_tools.salt.common.SStructuredNode;
import org.corpus_tools.salt.core.SLayer;
import org.corpus_tools.salt.core.SRelation;
import org.corpus_tools.salt.exceptions.SaltParameterException;
import org.corpus_tools.salt.graph.Identifier;
import org.corpus_tools.salt.util.TokenStartComparator;
import org.eclipse.emf.common.util.URI;
import org.osgi.service.component.annotations.Component;
import org.corpus_tools.salt.util.TokenStartComparator;
import org.corpus_tools.salt.util.internal.DataSourceAccessor;

/**
 * This is a {@link PepperManipulator} which attaches an arbitrary span type
 * to covered tokens which have a pointing relation coming from outside of the
 * span area. The practical purpose of this 
 * {@link PepperManipulator} is intended to be linking a markable span to 
 * its head token, assuming that a dependency graph is available for the tokens
 * and an incoming pointing relation from outside the span is indicative of 
 * head status.
 * 
 * @author Amir_Zeldes
 */
@Component(name = "HeadMarkerModuleManipulatorComponent", factory = "PepperManipulatorComponentFactory")
public class HeadMarkerModuleManipulator extends PepperManipulatorImpl {

    public HeadMarkerModuleManipulator() {
		super();
		setName("HeadMarker");
		setSupplierContact(URI.createURI(PepperConfiguration.EMAIL));
		setSupplierHomepage(URI.createURI(PepperConfiguration.HOMEPAGE));
		setDesc("This manipulator attaches spans to tokens with incoming pointing relations coming from outside the span");
	}

	/**
	 * @param Identifier
	 *            {@link Identifier} of the {@link SCorpus} or {@link SDocument}
	 *            to be processed.
	 * @return {@link PepperMapper} object to do the mapping task for object
	 *         connected to given {@link Identifier}
	 */
	public PepperMapper createPepperMapper(Identifier Identifier) {
		HeadMarkerModuleMapper mapper = new HeadMarkerModuleMapper();
		return (mapper);
	}


	public static class HeadMarkerModuleMapper extends PepperMapperImpl implements GraphTraverseHandler {

                @Override
		public DOCUMENT_STATUS mapSCorpus() {
			return (DOCUMENT_STATUS.COMPLETED);
		}

		@Override
		public DOCUMENT_STATUS mapSDocument() {

                        //HeadMarkerModuleManipulatorProperties properties = (HeadMarkerModuleManipulatorProperties) this.getProperties();                    
                        
                        // set up module properties
                        String spanLayer = (String) getProperties().getProperties().getProperty(HeadMarkerModuleManipulatorProperties.SPANLAYER, "");
                        String spanAnnotation = (String) getProperties().getProperties().getProperty(HeadMarkerModuleManipulatorProperties.SPANANNOTATION, "");
                        String edgeType = (String) getProperties().getProperties().getProperty(HeadMarkerModuleManipulatorProperties.EDGETYPE, "dep");
                        String edgeAnnotation = (String) getProperties().getProperties().getProperty(HeadMarkerModuleManipulatorProperties.EDGEANNOTATION, "func");
                        String ignoreEdgeAnnotation = (String) getProperties().getProperties().getProperty(HeadMarkerModuleManipulatorProperties.IGNOREEDGEANNOTATION, "punct");
                        String headRelType = (String) getProperties().getProperties().getProperty(HeadMarkerModuleManipulatorProperties.HEADRELTYPE, "head");
                        String headRelLayerName = (String) getProperties().getProperties().getProperty(HeadMarkerModuleManipulatorProperties.HEADRELLAYERNAME, "head");
                        boolean useDominanceRelations = Boolean.valueOf(getProperties().getProperties().getProperty(HeadMarkerModuleManipulatorProperties.USEDOM));
                        boolean ignoreRoots = Boolean.valueOf(getProperties().getProperties().getProperty(HeadMarkerModuleManipulatorProperties.IGNOREROOT));


                        //boolean usePointingRelations = properties.getUsePR();
                        //boolean useRoots = properties.getUseRoot();
                        
                        SLayer headRelLayer = null;
                        if (headRelLayerName != ""){
                            headRelLayer = SaltFactory.createSLayer();
                            headRelLayer.setName(headRelLayerName);
                        }

                        // set up comparator to sort tokens later    
                        TokenStartComparator comparator = new TokenStartComparator();
			comparator.setDocumentGraph(getDocument().getDocumentGraph());

                        List<SSpan> spans = getDocument().getDocumentGraph().getSpans();

                        for (SSpan span : spans){
                            if (spanAnnotation != ""){ // if some annotation is required, check that this span has it
                                Set<SAnnotation> annos = span.getAnnotations();
                                boolean found = false;
                                for (SAnnotation anno  : annos){ // if some annotation is required, check that this span has it
                                    if (anno.getName().equals(spanAnnotation)){
                                        found = true;
                                    }
                                }
                                if (! found){
                                    if (true){
                                        throw new PepperModuleDataException(this, "no anno");
                                    }
                                    continue;
                                }                                
                                
                            }
                            if (spanLayer != ""){
                                Set<SLayer> layers = span.getLayers();
                                boolean found = false;
                                for (SLayer layer : layers){ // if some layer is required, check that this span has it
                                    if (layer.getName().equals(spanLayer)){
                                        found = true;
                                    }
                                    if (true){
                                        //throw new PepperModuleDataException(this, "layer: " + layer.getName());
                                    }

                                }
                                if (! found){
                                    if (true){
                                        throw new PepperModuleDataException(this, "no layer - " + layers.toString());
                                    }
                                    continue;
                                }
                            }
                            
                            // if we got this far, this is a targeted span
                            
                            // get span children and case as list of SToken
                            List<SToken> tokens = DataSourceAccessor.getOverlappedSTokens(getDocument().getDocumentGraph(), span, SALT_TYPE.SSPANNING_RELATION);
                            //List<SNode> tokensAsNodes = span.getGraph().getChildren(span, SALT_TYPE.STOKEN);
                            //List<SToken> tokens = (List<SToken>)(List<?>) tokensAsNodes;

                            // sort tokens
                            List<SToken> sortedTokens = getSortedSTokenByText(getDocument().getDocumentGraph(),tokens);

                            SToken startToken = sortedTokens.get(0);
                            SToken endToken = sortedTokens.get(sortedTokens.size() - 1);
                            
                            boolean found = false;
                            for (SNode token: sortedTokens){
                                if (found){
                                    found = false;
                                    break;
                                }
                                List<SRelation> rels = token.getInRelations();
                                List<SPointingRelation> pRels = new ArrayList<>();
                                for (SRelation rel: rels){
                                    if (rel instanceof SPointingRelation){
                                        if (edgeTypeMatch(rel,edgeType,edgeAnnotation,ignoreEdgeAnnotation)){
                                            pRels.add((SPointingRelation) rel);
                                        }
                                    }                                
                                }
                                if (pRels.size() == 0 && ! ignoreRoots){ // token with no relevant incoming PR - if we don't ignore roots then this is the head
                                    addRel(span,(SToken) token,getDocument().getDocumentGraph(),headRelType,headRelLayer,useDominanceRelations);                                    
                                    break;
                                }
                                else{
                                    for (SPointingRelation rel: pRels){
                                        // incoming pointing relation found on token!
                                        SNode source = ((SPointingRelation) rel).getSource();
                                        if (source instanceof SToken){  // Check that this can be a token with a dependency annotation
                                            if (comparator.compare((SToken) source, endToken) > 0){
                                                // source is greater than end token, this is potentially the head
                                                found = true;                                                    
                                            }
                                            else if (comparator.compare((SToken) source, startToken) < 0){
                                                // source is less than start token, this is potentially the head
                                                found = true;                                                    
                                            }
                                            if (found){
                                                addRel(span,(SToken) token,getDocument().getDocumentGraph(),headRelType,headRelLayer,useDominanceRelations);
                                            }
                                        }                                                                                                                                                                                                                                 
                                    }         
                                }
                            }                            
                        }
                        

			return (DOCUMENT_STATUS.COMPLETED);
		}

		/**
		 * This method is called for each node in document-structure, as long as
		 * {@link #checkConstraint(GRAPH_TRAVERSE_TYPE, String, SRelation, SNode, long)}
		 * returns true for this node. <br/>
		 */
		@Override
		public void nodeReached(GRAPH_TRAVERSE_TYPE traversalType, String traversalId, SNode currNode, SRelation sRelation, SNode fromNode, long order) {

		}

		/**
		 * This method is called on the way back, in depth first mode it is
		 * called for a node after all the nodes belonging to its subtree have
		 * been visited. <br/>
		 * In our dummy implementation, this method is not used.
		 */
		@Override
		public void nodeLeft(GRAPH_TRAVERSE_TYPE traversalType, String traversalId, SNode currNode, SRelation edge, SNode fromNode, long order) {
		}

		/**
		 * With this method you can decide if a node is supposed to be visited
		 * by methods
		 * {@link #nodeReached(GRAPH_TRAVERSE_TYPE, String, SNode, SRelation, SNode, long)}
		 * and
		 * {@link #nodeLeft(GRAPH_TRAVERSE_TYPE, String, SNode, SRelation, SNode, long)}
		 * . In our dummy implementation for instance we do not need to visit
		 * the nodes {@link STextualDS}.
		 */
		@Override
		public boolean checkConstraint(GRAPH_TRAVERSE_TYPE traversalType, String traversalId, SRelation edge, SNode currNode, long order) {
			if (currNode instanceof STextualDS) {
				return (false);
			} else {
				return (true);
			}
		}
	}

	/**
	 * This method is called by the pepper framework after initializing this
	 * object and directly before start processing. Initializing means setting
	 * properties {@link PepperModuleProperties}, setting temporary files,
	 * resources etc. . returns false or throws an exception in case of
	 * {@link PepperModule} instance is not ready for any reason.
	 * 
	 * @return false, {@link PepperModule} instance is not ready for any reason,
	 *         true, else.
	 */
	@Override
	public boolean isReadyToStart() throws PepperModuleNotReadyException {
		// TODO make some initializations if necessary
		//return (super.isReadyToStart());
                return true;
	}
        
        
        private static boolean edgeTypeMatch(SRelation rel, String type, String anno, String ignore){
            
            boolean found = false;
            if (!"".equals(type) && ! rel.getType().equals(type)){
                return false;
            }
            else if (!"".equals(anno)){
                Set<SAnnotation> annos = rel.getAnnotations();
                for (SAnnotation a : annos){
                    if (a.getName().equals(anno)){
                        if (a.getValue_STEXT().equals(ignore)){
                            return false;
                        }
                        else{
                            found = true;
                        }
                    }
                }
                if (!found){
                    return false;
                }                
            }
            return true;
        }
        
        	/**
	 * {@inheritDoc SDocumentGraph#getSortedTokenByText(List)}
	 */
	public static List<SToken> getSortedSTokenByText(SDocumentGraph documentGraph, List<SToken> sTokens2sort) {
		if (documentGraph == null) {
			throw new SaltParameterException("Cannot start method please set the document graph first.");
		}
		List<SToken> retVal = null;
		if (sTokens2sort != null) {
			TokenStartComparator comparator = new TokenStartComparator();
			comparator.setDocumentGraph(documentGraph);
			retVal = new ArrayList<SToken>();
			retVal.addAll(sTokens2sort);
			// sort tokens
			Collections.sort(retVal, comparator);
		}
		return (retVal);
	}
        
        public static void addRel(SSpan span, SToken token, SDocumentGraph graph, 
                String headRelType, SLayer headRelLayer, boolean useDominanceRelations){
            SRelation newRel;
            if (useDominanceRelations){
                newRel = SaltFactory.createSDominanceRelation();                                                        
            }
            else{
                newRel = SaltFactory.createSPointingRelation();
            }
            newRel.setSource(span);
            newRel.setTarget((SStructuredNode) token);
            newRel.setType(headRelType);
            if (headRelLayer != null){
                newRel.addLayer(headRelLayer);
            }
            graph.addRelation(newRel);
        }
                
        
}
