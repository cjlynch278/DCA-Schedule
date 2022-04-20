import com.collibra.dgc.core.api.model.instance.attribute.Attribute
import com.collibra.dgc.core.api.dto.instance.attribute.FindAttributesRequest
import com.collibra.dgc.core.api.dto.instance.attribute.FindAttributesRequest.Builder
import com.collibra.dgc.core.api.dto.instance.asset.FindAssetsRequest
import com.collibra.dgc.core.api.dto.instance.asset.FindAssetsRequest.Builder
import com.collibra.dgc.core.api.model.instance.Domain
import com.collibra.dgc.core.api.dto.instance.domain.FindDomainsRequest
import com.collibra.dgc.core.api.dto.instance.domain.FindDomainsRequest.Builder
import static com.collibra.dgc.core.api.dto.SortOrder.ASC
import static com.collibra.dgc.core.api.dto.instance.attribute.FindAttributesRequest.SortField.LAST_MODIFIED
import com.collibra.dgc.core.api.component.instance.AssetApi
import com.collibra.dgc.core.api.model.instance.Asset
import com.collibra.dgc.core.api.component.instance.RelationApi
import com.collibra.dgc.core.api.component.logger.LoggerApi
import com.collibra.dgc.core.api.dto.instance.relation.FindRelationsRequest
import com.collibra.dgc.core.api.dto.instance.relation.FindRelationsRequest.Builder
import com.collibra.dgc.core.api.component.meta.AssetTypeApi
import com.collibra.dgc.core.api.component.meta.AttributeTypeApi
import com.collibra.dgc.core.api.dto.instance.complexrelation.AttributeValue
import com.collibra.dgc.core.api.dto.meta.assettype.FindAssetTypesRequest
import com.collibra.dgc.core.api.dto.meta.assettype.FindAssetTypesRequest.Builder
import com.collibra.dgc.core.api.model.meta.type.AssetType
import com.collibra.dgc.core.api.dto.instance.asset.SetAssetAttributesRequest.Builder
import com.collibra.dgc.core.api.dto.instance.asset.SetAssetAttributesRequest
import com.collibra.dgc.core.api.dto.instance.attribute.ChangeAttributeRequest


def metricAttributeId = string2Uuid(execution.getVariable("metricAttributeId"))
def uniqueIdentifierId = string2Uuid(execution.getVariable("uniqueIdentifierId"))
def multiSystemId = string2Uuid(execution.getVariable("multiSystemId"))
def hierarchicalId = string2Uuid(execution.getVariable("hierarchicalId"))
def dataTypeId = string2Uuid(execution.getVariable("dataTypeId"))
def mdeIDKey = execution.getVariable("mdeIDKey")
def sdeIDKey = execution.getVariable("sdeIDKey")


//Keys for relationships
def UIFRelationship = execution.getVariable("UIFRelationship")
def columnRelationship = execution.getVariable("columnRelationship")

def domainList = domainApi.findDomains(FindDomainsRequest.builder()
	.typeId(string2Uuid(PDDTypeID))
	.includeSubCommunities(true)
	.communityId(string2Uuid(communityID))
	.build())
	.getResults()

loggerApi.info("Domains: " + domainList)

def assetList = []
for (domain in domainList) {
	loggerApi.info("Domain ID: " + domain.getId())
	assetList += assetApi.findAssets(FindAssetsRequest.builder()
	.domainId(domain.getId())
	.typeIds([string2Uuid(UIFTypeID), string2Uuid(columnTypeID)]).build())
	.getResults()
	loggerApi.info("Current AssetList: " + assetList)
	
}



loggerApi.info("Total asset List : " + assetList)

for( thisAsset in assetList) {

def sdeBool = false
def mdeBool = false
def kpiBool = false
def uniqueIdBool= false
def multiSystemBool = false
def hierarchicalBool = false


//Get all source relations of currently selected asset
def relationsResponse = relationApi.findRelations(
	FindRelationsRequest.builder()
		.sourceId(thisAsset.id)
		.build()
	)

for(relation in relationsResponse.getResults()) {
	
	Asset currentAsset = assetApi.getAsset(relation.target.id)
	currentAssetType = currentAsset.getType().getId().toString()
	loggerApi.info("CurrentAsset type : " + currentAssetType)
	loggerApi.info("SDE Key " + sdeIDKey)
	loggerApi.info("Current Relation Type: " + relation.getType().getId().toString())
	
	uifOrColumRelationBool = false
	if(relation.getType().getId().toString() == UIFRelationship || relation.getType().getId().toString() == columnRelationship) {
		uifOrColumRelationBool = true
	}
	loggerApi.info("uifOrColumRelationBool: " + uifOrColumRelationBool)
	
	currentAssetStatusBool = false
	if(currentAsset.getStatus().getName().toString() == 'Approved' || currentAsset.getStatus().getName().toString() == 'Accepted') {
		loggerApi.info("Flipping status bool to true")
		currentAssetStatusBool = true
	}
	loggerApi.info("currentAssetStatusBool: " + currentAssetStatusBool)
		
	if(currentAssetType == mdeIDKey && currentAssetStatusBool && uifOrColumRelationBool) {
		loggerApi.info("Switching mdeBool to True")
		mdeBool = true
	}
	else if(currentAssetType == sdeIDKey && currentAssetStatusBool && uifOrColumRelationBool) {
		loggerApi.info("Switching sdeBool to True")
		sdeBool = true
	}
	
}

//Get all target relations of currently selected asset
def targetRelationsResponse = relationApi.findRelations(
	FindRelationsRequest.builder()
		.targetId(thisAsset.id)
		.build()
	)

for(relation in targetRelationsResponse.getResults()) {
	
	Asset currentAsset = assetApi.getAsset(relation.source.id)
	currentAssetType = currentAsset.getType().getId().toString()
	loggerApi.info("CurrentAsset type : " + currentAssetType)
	loggerApi.info("SDE Key " + sdeIDKey)
	loggerApi.info("Current Relation Type: " + relation.getType().getId().toString())
	
	uifOrColumRelationBool = false
	if(relation.getType().getId().toString() == UIFRelationship || relation.getType().getId().toString() == columnRelationship) {
		uifOrColumRelationBool = true
	}
	loggerApi.info("uifOrColumRelationBool: " + uifOrColumRelationBool)
	
	currentAssetStatusBool = false
	if(currentAsset.getStatus().getName().toString() == 'Approved' || currentAsset.getStatus().getName().toString() == 'Accepted') {
		loggerApi.info("Flipping status bool to true")
		currentAssetStatusBool = true
	}
	
	loggerApi.info("currentAssetStatusBool: " + currentAssetStatusBool)
	
	if(currentAssetType == mdeIDKey && currentAssetStatusBool && uifOrColumRelationBool) {
		loggerApi.info("Switching mdeBool to True")
		mdeBool = true
	}
	else if(currentAssetType == sdeIDKey && currentAssetStatusBool && uifOrColumRelationBool) {
		loggerApi.info("Switching sdeBool to True")
		sdeBool = true
	}
	
}


//Get Used in Enterprise Metric or KPI attribute
kpiBool = getAttribute(metricAttributeId, thisAsset)
loggerApi.info("KPI Bool: " + kpiBool)

uniqueIdBool = getAttribute(uniqueIdentifierId, thisAsset)
loggerApi.info("Unique ID Bool: " + uniqueIdBool)

multiSystemBool = getAttribute(multiSystemId, thisAsset)
loggerApi.info("MultiSystem Bool: " + multiSystemBool)

hierarchicalBool = getAttribute(hierarchicalId, thisAsset)
loggerApi.info("Hierarchical Bool: " + hierarchicalBool)

loggerApi.info("Sde Bool: " + sdeBool)
loggerApi.info("MDE Bool: " + mdeBool)


if( mdeBool == false && sdeBool == false && kpiBool == null && multiSystemBool == null && uniqueIdBool == null && hierarchicalBool == null)
	setDcaAttribute("Needs Assessment", thisAsset)
else if(mdeBool)
	setDcaAttribute("Enterprise Master Data", thisAsset)
else if(sdeBool)
	setDcaAttribute("Enterprise Shared Data", thisAsset)
else if(kpiBool)
	setDcaAttribute("Enterprise Master Data", thisAsset)
else if(multiSystemBool == false)
	setDcaAttribute("Single Application Data", thisAsset)
else if(multiSystemBool == null)
	setDcaAttribute("Needs Assessment", thisAsset)
else if(uniqueIdBool)
	setDcaAttribute("Enterprise Master Data", thisAsset)
else if(hierarchicalBool)
	setDcaAttribute("Enterprise Master Data", thisAsset)
else if(!hierarchicalBool )
	setDcaAttribute("Enterprise Shared Data", thisAsset)
else
	loggerApi.warn("No logic exists to change DCA type")

}

def setDcaAttribute(dataValue, thisAsset) {
	
	assetApi.setAssetAttributes(
		SetAssetAttributesRequest.builder()
			.assetId(thisAsset.id)
			.values([dataValue])
			.typeId(string2Uuid(dataCriticalityAttributeId))
			.build()
		)
}
def changeAttribute(dataTypeAttributeId, dataValue) {
	attributeApi.changeAttribute(
		ChangeAttributeRequest.builder()
			.id(dataTypeAttributeId)
			.value(dataValue)
			.build()
		)
}
def getAttribute(attributeId, thisAsset) {
	try {
		assetAttributesResponse = attributeApi.findAttributes(
			FindAttributesRequest.builder()
				.assetId(thisAsset.id)
				.typeIds([attributeId])
				.build()
		)
		loggerApi.info("Attributes Response: " + assetAttributesResponse)
		return assetAttributesResponse.results[0].value
	
	} catch(Exception e) {
		loggerApi.info("Exception: " + e)
		return null
	}
	
}


