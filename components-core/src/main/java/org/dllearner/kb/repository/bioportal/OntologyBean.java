/**
 * Copyright (C) 2007 - 2016, Jens Lehmann
 *
 * This file is part of DL-Learner.
 *
 * DL-Learner is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * DL-Learner is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.dllearner.kb.repository.bioportal;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Simplified version of the
 * <code>org.ncbo.stanford.bean.concept.OntologyBean</code> class used to decode
 * the "ontologyBean" element from a successful REST call to a BioPortal
 * ontology service
 * 
 * @author csnyulas
 * 
 */
public class OntologyBean implements Comparable<OntologyBean> {

	public static final String DEFAULT_SYNONYM_SLOT = "http://www.w3.org/2004/02/skos/core#altLabel";
	public static final String DEFAULT_PREFERRED_NAME_SLOT = "http://www.w3.org/2004/02/skos/core#prefLabel";
	public static final String DEFAULT_DEFINITION_SLOT = "http://www.w3.org/2004/02/skos/core#definition";
	public static final String DEFAULT_AUTHOR_SLOT = "http://purl.org/dc/elements/1.1/creator";

	private Integer id;
	private Integer ontologyId;
	// virtual view id(s) on the virtual ontology
	private List<Integer> virtualViewIds = new ArrayList<>(0);
	private Integer internalVersionNumber;
	private List<Integer> userIds;
	private String versionNumber;
	private String versionStatus;
	private Byte isRemote;
	private Byte isReviewed;
	private Integer statusId;
	private Date dateCreated;
	private Date dateReleased;
	private String oboFoundryId;
	private Byte isManual;
	private String displayLabel;
	private String description;
	private String abbreviation;
	private String format;
	private String contactName;
	private String contactEmail;
	private String homepage;
	private String documentation;
	private String publication;
	private String urn;
	private String codingScheme;
	private String targetTerminologies;
	private Byte isFlat;
	private Byte isFoundry;
	private Byte isMetadataOnly;
	private String synonymSlot;
	private String preferredNameSlot;
	private String documentationSlot;
	private String authorSlot;
	private String slotWithUniqueValue;
	private Integer preferredMaximumSubclassLimit;
	private String obsoleteParent;
	private String naturalLanguage;
	private String obsoleteProperty;
	private String licenseInformation;

	private String viewingRestriction;

	private List<UserEntry> userAcl = new ArrayList<>(0);

	private boolean isView = false;

	// category id(s)
	private List<Integer> categoryIds = new ArrayList<>(0);

	// group id(s)
	private List<Integer> groupIds = new ArrayList<>(0);

	// file name(s)
	private List<String> filenames = new ArrayList<>(0);

	// source fileItem
	// private FileItem fileItem;

	// destination directory
	private String filePath;

	// Download location of ontology
	private String downloadLocation;

	// views on this ontology version
	private List<Integer> hasViews = new ArrayList<>(0);

	// view specific properties
	private List<Integer> viewOnOntologyVersionId = new ArrayList<>(0);
	private String viewDefinition;
	private String viewDefinitionLanguage;
	private String viewGenerationEngine;

	@Override
	public String toString() {
		final int max = 80;
		String viewDef = this.getViewDefinition();

		if (viewDef != null && viewDef.length() > max) {
			viewDef = viewDef.substring(0, max) + "...";
		}

		String name = isView ? "OntologyView " : "Ontology ";

		return name + "{Id: " + this.getId() + ", Ontology Id: " + this.getOntologyId() + ", Virtual View Ids: "
				+ this.getVirtualViewIds() + ", Remote: " + this.getIsRemote() + ", Obo Foundry Id: "
				+ this.getOboFoundryId() + ", Internal Version Number: " + this.getInternalVersionNumber()
				+ ", Date Created: " + this.getDateCreated() + ", User Ids: " + this.getUserIds()
				+ ", Version Number: " + this.getVersionNumber() + ", Version Status: " + this.getVersionStatus()
				+ ", Display Label: " + this.getDisplayLabel() + ", Description: " + this.getDescription()
				+ ", Abbreviation: " + this.getAbbreviation() + ", Format: " + this.getFormat() + ", Contact Name: "
				+ this.getContactName() + ", Contact Email: " + this.getContactEmail() + ", Foundry: "
				+ this.getIsFoundry() + " Coding Scheme: " + this.getCodingScheme() + ", Target Terminologies: "
				+ this.getTargetTerminologies() + ", Synonym Slot: " + this.getSynonymSlot()
				+ ", Preferred Name Slot: " + this.getPreferredNameSlot() + ", View Definition: " + viewDef
				+ ", View Definition Language: " + this.getViewDefinitionLanguage() + ", View Generation Engine: "
				+ this.getViewGenerationEngine() + ", View on Ontology Versions: " + this.getViewOnOntologyVersionId()
				+ "}";
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public Integer getOntologyId() {
		return ontologyId;
	}

	public void setOntologyId(Integer ontologyId) {
		this.ontologyId = ontologyId;
	}

	public List<Integer> getVirtualViewIds() {
		return virtualViewIds;
	}

	public void setVirtualViewIds(List<Integer> virtualViewIds) {
		this.virtualViewIds = virtualViewIds;
	}

	public Integer getInternalVersionNumber() {
		return internalVersionNumber;
	}

	public void setInternalVersionNumber(Integer internalVersionNumber) {
		this.internalVersionNumber = internalVersionNumber;
	}

	public List<Integer> getUserIds() {
		return userIds;
	}

	public void setUserIds(List<Integer> userIds) {
		this.userIds = userIds;
	}

	public String getVersionNumber() {
		return versionNumber;
	}

	public void setVersionNumber(String versionNumber) {
		this.versionNumber = versionNumber;
	}

	public String getVersionStatus() {
		return versionStatus;
	}

	public void setVersionStatus(String versionStatus) {
		this.versionStatus = versionStatus;
	}

	public Byte getIsRemote() {
		return isRemote;
	}

	public void setIsRemote(Byte isRemote) {
		this.isRemote = isRemote;
	}

	public Byte getIsReviewed() {
		return isReviewed;
	}

	public void setIsReviewed(Byte isReviewed) {
		this.isReviewed = isReviewed;
	}

	public Integer getStatusId() {
		return statusId;
	}

	public void setStatusId(Integer statusId) {
		this.statusId = statusId;
	}

	public Date getDateCreated() {
		return dateCreated;
	}

	public void setDateCreated(Date dateCreated) {
		this.dateCreated = dateCreated;
	}

	public Date getDateReleased() {
		return dateReleased;
	}

	public void setDateReleased(Date dateReleased) {
		this.dateReleased = dateReleased;
	}

	public String getOboFoundryId() {
		return oboFoundryId;
	}

	public void setOboFoundryId(String oboFoundryId) {
		this.oboFoundryId = oboFoundryId;
	}

	public Byte getIsManual() {
		return isManual;
	}

	public void setIsManual(Byte isManual) {
		this.isManual = isManual;
	}

	public String getDisplayLabel() {
		return displayLabel;
	}

	public void setDisplayLabel(String displayLabel) {
		this.displayLabel = displayLabel;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getAbbreviation() {
		return abbreviation;
	}

	public void setAbbreviation(String abbreviation) {
		this.abbreviation = abbreviation;
	}

	public String getFormat() {
		return format;
	}

	public void setFormat(String format) {
		this.format = format;
	}

	public String getContactName() {
		return contactName;
	}

	public void setContactName(String contactName) {
		this.contactName = contactName;
	}

	public String getContactEmail() {
		return contactEmail;
	}

	public void setContactEmail(String contactEmail) {
		this.contactEmail = contactEmail;
	}

	public String getHomepage() {
		return homepage;
	}

	public void setHomepage(String homepage) {
		this.homepage = homepage;
	}

	public String getDocumentation() {
		return documentation;
	}

	public void setDocumentation(String documentation) {
		this.documentation = documentation;
	}

	public String getPublication() {
		return publication;
	}

	public void setPublication(String publication) {
		this.publication = publication;
	}

	public String getUrn() {
		return urn;
	}

	public void setUrn(String urn) {
		this.urn = urn;
	}

	public String getCodingScheme() {
		return codingScheme;
	}

	public void setCodingScheme(String codingScheme) {
		this.codingScheme = codingScheme;
	}

	public String getTargetTerminologies() {
		return targetTerminologies;
	}

	public void setTargetTerminologies(String targetTerminologies) {
		this.targetTerminologies = targetTerminologies;
	}

	public Byte getIsFlat() {
		return isFlat;
	}

	public void setIsFlat(Byte isFlat) {
		this.isFlat = isFlat;
	}

	public Byte getIsFoundry() {
		return isFoundry;
	}

	public void setIsFoundry(Byte isFoundry) {
		this.isFoundry = isFoundry;
	}

	public Byte getIsMetadataOnly() {
		return isMetadataOnly;
	}

	public void setIsMetadataOnly(Byte isMetadataOnly) {
		this.isMetadataOnly = isMetadataOnly;
	}

	public String getSynonymSlot() {
		return synonymSlot;
	}

	public void setSynonymSlot(String synonymSlot) {
		this.synonymSlot = synonymSlot;
	}

	public String getPreferredNameSlot() {
		return preferredNameSlot;
	}

	public void setPreferredNameSlot(String preferredNameSlot) {
		this.preferredNameSlot = preferredNameSlot;
	}

	public String getDocumentationSlot() {
		return documentationSlot;
	}

	public void setDocumentationSlot(String documentationSlot) {
		this.documentationSlot = documentationSlot;
	}

	public String getAuthorSlot() {
		return authorSlot;
	}

	public void setAuthorSlot(String authorSlot) {
		this.authorSlot = authorSlot;
	}

	public String getSlotWithUniqueValue() {
		return slotWithUniqueValue;
	}

	public void setSlotWithUniqueValue(String slotWithUniqueValue) {
		this.slotWithUniqueValue = slotWithUniqueValue;
	}

	public Integer getPreferredMaximumSubclassLimit() {
		return preferredMaximumSubclassLimit;
	}

	public void setPreferredMaximumSubclassLimit(Integer preferredMaximumSubclassLimit) {
		this.preferredMaximumSubclassLimit = preferredMaximumSubclassLimit;
	}

	public boolean isView() {
		return isView;
	}

	public void setView(boolean isView) {
		this.isView = isView;
	}

	public List<Integer> getCategoryIds() {
		return categoryIds;
	}

	public void setCategoryIds(List<Integer> categoryIds) {
		this.categoryIds = categoryIds;
	}

	public List<Integer> getGroupIds() {
		return groupIds;
	}

	public void setGroupIds(List<Integer> groupIds) {
		this.groupIds = groupIds;
	}

	public List<String> getFilenames() {
		return filenames;
	}

	public void setFilenames(List<String> filenames) {
		this.filenames = filenames;
	}

	public String getFilePath() {
		return filePath;
	}

	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}

	public List<Integer> getHasViews() {
		return hasViews;
	}

	public void setHasViews(List<Integer> hasViews) {
		this.hasViews = hasViews;
	}

	public List<Integer> getViewOnOntologyVersionId() {
		return viewOnOntologyVersionId;
	}

	public void setViewOnOntologyVersionId(List<Integer> viewOnOntologyVersionId) {
		this.viewOnOntologyVersionId = viewOnOntologyVersionId;
	}

	public String getViewDefinition() {
		return viewDefinition;
	}

	public void setViewDefinition(String viewDefinition) {
		this.viewDefinition = viewDefinition;
	}

	public String getViewDefinitionLanguage() {
		return viewDefinitionLanguage;
	}

	public void setViewDefinitionLanguage(String viewDefinitionLanguage) {
		this.viewDefinitionLanguage = viewDefinitionLanguage;
	}

	public String getViewGenerationEngine() {
		return viewGenerationEngine;
	}

	public void setViewGenerationEngine(String viewGenerationEngine) {
		this.viewGenerationEngine = viewGenerationEngine;
	}

	public static String getDefaultPreferredNameSlot() {
		return DEFAULT_PREFERRED_NAME_SLOT;
	}

	public static String getDefaultDefinitionSlot() {
		return DEFAULT_DEFINITION_SLOT;
	}

	public static String getDefaultAuthorSlot() {
		return DEFAULT_AUTHOR_SLOT;
	}

	public String getDownloadLocation() {
		return downloadLocation;
	}

	public void setDownloadLocation(String downloadLocation) {
		this.downloadLocation = downloadLocation;
	}

	/**
	 * @return the userAcl
	 */
	public List<UserEntry> getUserAcl() {
		return userAcl;
	}

	/**
	 * @param userAcl
	 *            the userAcl to set
	 */
	public void setUserAcl(List<UserEntry> userAcl) {
		this.userAcl = userAcl;
	}

	public String getViewingRestriction() {
		return viewingRestriction;
	}

	public void setViewingRestriction(String viewingRestriction) {
		this.viewingRestriction = viewingRestriction;
	}

	public String getObsoleteParent() {
		return obsoleteParent;
	}

	public void setObsoleteParent(String obsoleteParent) {
		this.obsoleteParent = obsoleteParent;
	}

	public String getNaturalLanguage() {
		return naturalLanguage;
	}

	public void setNaturalLanguage(String naturalLanguage) {
		this.naturalLanguage = naturalLanguage;
	}
	
	public String getObsoleteProperty() {
		return obsoleteProperty;
	}
	
	public void setObsoleteProperty(String obsoleteProperty) {
		this.obsoleteProperty = obsoleteProperty;
	}
	
	public String getLicenseInformation() {
		return licenseInformation;
	}
	
	public void setLicenseInformation(String licenseInformation) {
		this.licenseInformation = licenseInformation;
	}

	@Override
	public int compareTo(OntologyBean o) {
		return this.displayLabel.compareTo(o.getDisplayLabel());
	}

}
