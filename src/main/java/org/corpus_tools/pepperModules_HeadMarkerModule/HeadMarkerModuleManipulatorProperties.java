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
import org.corpus_tools.pepper.modules.PepperModuleProperties;
import org.corpus_tools.pepper.modules.PepperModuleProperty;
/**
 *
 * @author Amir Zeldes
 */
public class HeadMarkerModuleManipulatorProperties extends PepperModuleProperties {
    

	public static final String PREFIX = "HeadMarker.";


        public final static String SPANLAYER = PREFIX + "spanLayer";
	public final static String SPANANNOTATION = PREFIX + "spanAnnotation";
	public final static String EDGETYPE = PREFIX + "edgeType";
        public final static String EDGEANNOTATION = PREFIX + "edgeAnnotation";
	public final static String IGNOREEDGEANNOTATION = PREFIX + "ignoreEdgeAnnotation";
	public final static String HEADRELTYPE = PREFIX + "headRelType";
        public final static String HEADRELLAYERNAME = PREFIX + "headRelLayerName";
	public final static String USEDOM = PREFIX + "useDominanceRelations";
	public final static String IGNOREROOT = PREFIX + "ignoreRoot";

        
	public HeadMarkerModuleManipulatorProperties() {
            
		this.addProperty(new PepperModuleProperty<String>(SPANLAYER, String.class, "Specifies a layer which targeted spans must be in; if not specified, all spans are targeted.", "", false));
		this.addProperty(new PepperModuleProperty<String>(SPANANNOTATION, String.class, "Specifies an annotation which targeted spans must have; if not specified, all spans are targeted.", "", false));
		this.addProperty(new PepperModuleProperty<String>(EDGETYPE, String.class, "Specifies an edge type for incoming edges identifying the head.", "dep", false));
		this.addProperty(new PepperModuleProperty<String>(EDGEANNOTATION, String.class, "Specifies an incoming edge annotation to check for special ignore values, e.g. punctuation.", "func", false));
		this.addProperty(new PepperModuleProperty<String>(IGNOREEDGEANNOTATION, String.class, "Specifies annotation values on incoming edges which cause the edge to be ignored, e.g. punctuation.", "punct", false));
		this.addProperty(new PepperModuleProperty<String>(HEADRELTYPE, String.class, "Specifies the type to assign to the added edges.", "head", false));
		this.addProperty(new PepperModuleProperty<String>(HEADRELLAYERNAME, String.class, "Specifies a layer to give to added edges.", "", false));
		this.addProperty(new PepperModuleProperty<Boolean>(USEDOM, Boolean.class, "Whether to use dominance relations, false by default, then uses pointing relations.", false, false));
		this.addProperty(new PepperModuleProperty<Boolean>(IGNOREROOT, Boolean.class, "If true, then tokens with no incoming edges inside a targeted span are not considered possible heads (for cases where a root dependency is unmarked).", false, false));
        }

}
