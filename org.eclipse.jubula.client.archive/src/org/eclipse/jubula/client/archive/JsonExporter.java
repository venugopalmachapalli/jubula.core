/*******************************************************************************
 * Copyright (c) 2016 BREDEX GmbH.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BREDEX GmbH - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.jubula.client.archive;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.output.FileWriterWithEncoding;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.jubula.client.archive.dto.AutConfigDTO;
import org.eclipse.jubula.client.archive.dto.AutDTO;
import org.eclipse.jubula.client.archive.dto.CapDTO;
import org.eclipse.jubula.client.archive.dto.CategoryDTO;
import org.eclipse.jubula.client.archive.dto.CheckConfigurationDTO;
import org.eclipse.jubula.client.archive.dto.CompNamesDTO;
import org.eclipse.jubula.client.archive.dto.ComponentNameDTO;
import org.eclipse.jubula.client.archive.dto.DataSetDTO;
import org.eclipse.jubula.client.archive.dto.EventHandlerDTO;
import org.eclipse.jubula.client.archive.dto.EventTestCaseDTO;
import org.eclipse.jubula.client.archive.dto.ExecCategoryDTO;
import org.eclipse.jubula.client.archive.dto.MapEntryDTO;
import org.eclipse.jubula.client.archive.dto.MonitoringValuesDTO;
import org.eclipse.jubula.client.archive.dto.NamedTestDataDTO;
import org.eclipse.jubula.client.archive.dto.ObjectMappingDTO;
import org.eclipse.jubula.client.archive.dto.ObjectMappingProfileDTO;
import org.eclipse.jubula.client.archive.dto.OmCategoryDTO;
import org.eclipse.jubula.client.archive.dto.OmEntryDTO;
import org.eclipse.jubula.client.archive.dto.ParamDescriptionDTO;
import org.eclipse.jubula.client.archive.dto.ProjectDTO;
import org.eclipse.jubula.client.archive.dto.RefTestCaseDTO;
import org.eclipse.jubula.client.archive.dto.RefTestSuiteDTO;
import org.eclipse.jubula.client.archive.dto.ReportingRuleDTO;
import org.eclipse.jubula.client.archive.dto.ReusedProjectDTO;
import org.eclipse.jubula.client.archive.dto.TDManagerDTO;
import org.eclipse.jubula.client.archive.dto.TechnicalNameDTO;
import org.eclipse.jubula.client.archive.dto.TestCaseDTO;
import org.eclipse.jubula.client.archive.dto.TestDataCategoryDTO;
import org.eclipse.jubula.client.archive.dto.TestJobDTO;
import org.eclipse.jubula.client.archive.dto.TestSuiteDTO;
import org.eclipse.jubula.client.archive.dto.TestresultSummaryDTO;
import org.eclipse.jubula.client.archive.dto.UsedToolkitDTO;
import org.eclipse.jubula.client.archive.i18n.Messages;
import org.eclipse.jubula.client.archive.schema.ReentryProperty;
import org.eclipse.jubula.client.core.businessprocess.ComponentNamesBP;
import org.eclipse.jubula.client.core.businessprocess.ProjectNameBP;
import org.eclipse.jubula.client.core.businessprocess.UsedToolkitBP;
import org.eclipse.jubula.client.core.model.IALMReportingRulePO;
import org.eclipse.jubula.client.core.model.IAUTConfigPO;
import org.eclipse.jubula.client.core.model.IAUTMainPO;
import org.eclipse.jubula.client.core.model.ICapPO;
import org.eclipse.jubula.client.core.model.ICapParamDescriptionPO;
import org.eclipse.jubula.client.core.model.ICategoryPO;
import org.eclipse.jubula.client.core.model.ICheckConfContPO;
import org.eclipse.jubula.client.core.model.ICheckConfPO;
import org.eclipse.jubula.client.core.model.ICompIdentifierPO;
import org.eclipse.jubula.client.core.model.ICompNamesPairPO;
import org.eclipse.jubula.client.core.model.IComponentNamePO;
import org.eclipse.jubula.client.core.model.IDataSetPO;
import org.eclipse.jubula.client.core.model.IEventExecTestCasePO;
import org.eclipse.jubula.client.core.model.IExecTestCasePO;
import org.eclipse.jubula.client.core.model.IObjectMappingAssoziationPO;
import org.eclipse.jubula.client.core.model.IObjectMappingCategoryPO;
import org.eclipse.jubula.client.core.model.IObjectMappingPO;
import org.eclipse.jubula.client.core.model.IObjectMappingProfilePO;
import org.eclipse.jubula.client.core.model.IParamDescriptionPO;
import org.eclipse.jubula.client.core.model.IParameterInterfacePO;
import org.eclipse.jubula.client.core.model.IProjectPO;
import org.eclipse.jubula.client.core.model.IProjectPropertiesPO;
import org.eclipse.jubula.client.core.model.IRefTestSuitePO;
import org.eclipse.jubula.client.core.model.IReusedProjectPO;
import org.eclipse.jubula.client.core.model.ISpecTestCasePO;
import org.eclipse.jubula.client.core.model.ITDManager;
import org.eclipse.jubula.client.core.model.ITestDataCategoryPO;
import org.eclipse.jubula.client.core.model.ITestDataCubePO;
import org.eclipse.jubula.client.core.model.ITestJobPO;
import org.eclipse.jubula.client.core.model.ITestResultSummaryPO;
import org.eclipse.jubula.client.core.model.ITestResultSummaryPO.AlmReportStatus;
import org.eclipse.jubula.client.core.model.ITestSuitePO;
import org.eclipse.jubula.client.core.model.IUsedToolkitPO;
import org.eclipse.jubula.client.core.persistence.IExecPersistable;
import org.eclipse.jubula.client.core.persistence.ISpecPersistable;
import org.eclipse.jubula.client.core.persistence.PMException;
import org.eclipse.jubula.client.core.persistence.PMSaveException;
import org.eclipse.jubula.client.core.persistence.TestResultSummaryPM;
import org.eclipse.jubula.client.core.utils.TrackingUnit;
import org.eclipse.jubula.tools.internal.constants.StringConstants;
import org.eclipse.jubula.tools.internal.exception.ProjectDeletedException;
import org.eclipse.jubula.tools.internal.messagehandling.MessageIDs;
import org.eclipse.jubula.tools.internal.objects.IMonitoringValue;
import org.eclipse.jubula.tools.internal.objects.MonitoringValue;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * @author BREDEX GmbH
 */
public class JsonExporter {
    
    /** offset of test result selection */
    public static final int PAGE_SIZE = 1000;
    
    /** the main DTO wat we need to export */
    private ProjectDTO m_projectDTO;
    
    /** export project */
    private IProjectPO m_project;
    
    /** loader monitor */
    private IProgressMonitor m_monitor;
    
    /**
     * @param project what we need to export
     * @param monitor loader monitor
     */
    public JsonExporter(IProjectPO project, IProgressMonitor monitor) {
        m_monitor = monitor;
        m_project = project;
    }
    
    /**
     * projectDTO contained whole project except test result summaries
     * 
     * @return a projectDTO
     * @throws ProjectDeletedException
     * @throws PMException
     * @throws OperationCanceledException 
     */
    public ProjectDTO getProjectDTO() throws ProjectDeletedException,
            PMException, OperationCanceledException {
        
        m_monitor.subTask(Messages.ExportProjectInfo);
        m_monitor.worked(1);
        m_projectDTO = new ProjectDTO(m_project);
        IProjectPropertiesPO projectProperties =
                m_project.getProjectProperties();
        fillCheckConfiguration(projectProperties.getCheckConfCont());
        fillUsedToolkits();
        fillComponentNames();
        m_projectDTO.setAutToolKit(m_project.getToolkit());
        fillTestDataCategories();
        fillNamedTestData();
        fillAUT();
        handleSpecPersistables();
        handleExecPersistables();
        m_monitor.worked(1); // nodes
        handleReusedProjects();
        setProjectVersions();
        m_projectDTO.setReusable(m_project.getIsReusable());
        m_projectDTO.setProtected(m_project.getIsProtected());
        m_projectDTO.setTeststyleEnabled(
                projectProperties.getCheckConfCont().getEnabled());
        m_projectDTO.setTestResultDetailsCleanupInterval(
                m_project.getTestResultCleanupInterval());
        fillALM(projectProperties);
        m_projectDTO.setReportOnSuccess(projectProperties
                .getIsReportOnSuccess());
        m_projectDTO.setReportOnFailure(projectProperties
                .getIsReportOnFailure());
        m_projectDTO.setDashboardURL(projectProperties.getDashboardURL());
        m_projectDTO.setMarkupLanguage(projectProperties.getMarkupLanguage());
        fillTrackingConfig(projectProperties);
        m_monitor.worked(1);
        
        return m_projectDTO;
    }
    
    /**
     * @param fWriter where the test result will be wrote 
     * @throws IOException
     * @throws PMException
     * @throws OperationCanceledException 
     */
    public void writeTestResultSummariesToFile(FileWriterWithEncoding fWriter)
            throws IOException, PMException, OperationCanceledException {
        
        ObjectMapper mapper = new ObjectMapper();
        mapper.setSerializationInclusion(Include.NON_EMPTY);

        fWriter.append(StringConstants.LEFT_BRACKET); // [
        float size = TestResultSummaryPM.countOfTestResultSummaries(
                m_project, null);
        long to = (long) Math.ceil(size / PAGE_SIZE);

        int pos = 0;
        for (int i = 1; i <= to; i++) {
            checkCancel();
            List<ITestResultSummaryPO> summaries = 
                    TestResultSummaryPM.getTestResultSummaries(
                            m_project, null, i, PAGE_SIZE);
            int k = 1;
            for (TestresultSummaryDTO dto
                    : getTestResultSummary(summaries)) {
                pos++;
                StringBuffer buffer = new StringBuffer();
                buffer.append(mapper.writeValueAsString(dto));
                if (!(i == to && k == summaries.size())) {
                    buffer.append(StringConstants.COMMA); // ,
                }
                fWriter.append(buffer);
                k++;
                m_monitor.worked(1);
            }
            m_monitor.subTask(Messages.ImportJsonImportResult + pos
                    + StringConstants.SLASH + (int) size);
        }

        fWriter.append(StringConstants.RIGHT_BRACKET); // ]
    }
    
    /**
     *  fill the check configuration part of projectDTO
     *  @param checkConfCont check configuration onjects
     * @throws OperationCanceledException 
     */
    private void fillCheckConfiguration(ICheckConfContPO checkConfCont)
            throws OperationCanceledException {
        checkCancel();
        for (String chkId : checkConfCont.getConfMap().keySet()) {
            ICheckConfPO chkConf = checkConfCont.getConfMap().get(chkId);
            CheckConfigurationDTO chkDTO = new CheckConfigurationDTO(chkConf);
            chkDTO.setCheckId(chkId);
            m_projectDTO.addCheckConfiguration(chkDTO);
        }
    }
    
    /** fill the used tool kits */
    private void fillUsedToolkits()
            throws ProjectDeletedException, PMSaveException {

        UsedToolkitBP toolkitBP = UsedToolkitBP.getInstance();
        try {
            toolkitBP.refreshToolkitInfo(m_project);
        } catch (PMException e) {
            throw new PMSaveException(
                    Messages.DataBaseErrorUpdatingToolkits
                            + String.valueOf(m_project.getName())
                            + Messages.OriginalException + e.toString(),
                            MessageIDs.E_FILE_IO);
        }
        fillUsedToolkit(toolkitBP);
    }
    
    /** 
     * set the used tool kit 
     * @param toolkitBP 
     */
    private void fillUsedToolkit(UsedToolkitBP toolkitBP) {
        final Set<IUsedToolkitPO> toolkits = toolkitBP.getUsedToolkits();
        for (IUsedToolkitPO usedToolkit : toolkits) {
            UsedToolkitDTO utDTO = new UsedToolkitDTO();
            utDTO.setName(usedToolkit.getToolkitId());
            utDTO.setMajorVersion(usedToolkit.getMajorVersion());
            utDTO.setMinorVersion(usedToolkit.getMinorVersion());
            m_projectDTO.addUsedToolkit(utDTO);
        }
    }

    /** fill the component names 
     * @throws OperationCanceledExceptiondException */
    private void fillComponentNames()
            throws PMException, OperationCanceledException {
        checkCancel();
        final Collection<IComponentNamePO> allCompNamePOs = ComponentNamesBP
                .getInstance().getAllComponentNamePOs(m_project.getId());
        for (IComponentNamePO compName : allCompNamePOs) {
            ComponentNameDTO compNameDTO = new ComponentNameDTO();
            compNameDTO.setGuid(compName.getGuid());
            compNameDTO.setCompType(compName.getComponentType());
            compNameDTO.setCompName(compName.getName());
            compNameDTO.setCreationContext(compName.getCreationContext()
                    .toString());
            compNameDTO.setRefGuid(compName.getReferencedGuid());
            m_projectDTO.addComponentName(compNameDTO);
        }
        m_monitor.worked(1);
    }

    /** fill the user tool kits 
     * @throws OperationCanceledException */
    private void fillTestDataCategories() throws OperationCanceledException {
        checkCancel();
        for (ITestDataCategoryPO testDataCategory 
                : m_project.getTestDataCubeCont().getCategoryChildren()) {
            TestDataCategoryDTO tdcDTO = new TestDataCategoryDTO(); 
            fillTestDataCategory(tdcDTO, testDataCategory);
            m_projectDTO.addTestDataCategorie(tdcDTO);
        }
    }

    /** 
     * fill the user tool kits
     * @param tdcDTO test data category dto what will contained the tdc item
     * @param tdc test data category
     */
    private void fillTestDataCategory(TestDataCategoryDTO tdcDTO,
            ITestDataCategoryPO tdc) {
        tdcDTO.setName(tdc.getName());
        for (ITestDataCategoryPO category : tdc.getCategoryChildren()) {
            TestDataCategoryDTO childTdcDTO = new TestDataCategoryDTO(); 
            fillTestDataCategory(childTdcDTO, category);
            tdcDTO.addTestDataCategorie(childTdcDTO);
        }
        for (ITestDataCubePO testData : tdc.getTestDataChildren()) {
            fillNamedTestData(testData);
        }
    }

    /** fill the name test dates */
    private void fillNamedTestData() {
        for (IParameterInterfacePO testDataCube 
                : m_project.getTestDataCubeCont().getTestDataChildren()) {
            fillNamedTestData(testDataCube);
        }
    }

    /**
     *  fill the name test data 
     *  @param testData   
     */
    private void fillNamedTestData(IParameterInterfacePO testData) {
        NamedTestDataDTO ntdDTO = new NamedTestDataDTO();
        ntdDTO.setName(testData.getName());
        for (IParamDescriptionPO paramDesc : testData.getParameterList()) {
            ParamDescriptionDTO pdDTO = new ParamDescriptionDTO();
            fillParamDescription(pdDTO, paramDesc);
            ntdDTO.addParameterDescriptions(pdDTO);
        }
        if (testData.getReferencedDataCube() == null) {
            TDManagerDTO tdmDTO = new TDManagerDTO();
            fillTDManager(tdmDTO, testData.getDataManager());
            ntdDTO.setTDManager(tdmDTO);
        }
        m_projectDTO.addNamedTestData(ntdDTO);
    }

    /**
     *  fill the parameter descriptions
     *  @param pdDTO will be contained the parameter description info
     *  @param po parameter description object  
     */
    private void fillParamDescription(ParamDescriptionDTO pdDTO,
            IParamDescriptionPO po) {
        if (po instanceof ICapParamDescriptionPO) {
            pdDTO.setName(po.getUniqueId());
        } else {
            pdDTO.setName(po.getName());
        }
        pdDTO.setType(po.getType());
        pdDTO.setUniqueId(po.getUniqueId());
    }
    
    /**
     *  @param dto will be contained the test data manager infos
     *  @param po data manager  
     */
    private void fillTDManager(TDManagerDTO dto, ITDManager po) {
        dto.setUniqueIds(po.getUniqueIds());
        for (IDataSetPO dataSet : po.getDataSets()) {
            DataSetDTO dsDTO = new DataSetDTO();
            dsDTO.setColumns(dataSet.getColumnsCopy());
            dto.addDataSet(dsDTO);
        }
    }
    
    /** fill the AUTs */
    private void fillAUT() {
        for (IAUTMainPO aut : getSortedAutList(m_project)) {
            fillAUT(aut);
        }
        m_monitor.worked(1);
    }

    /** 
     * set the AUT
     * @param po AUT object  
     */
    private void fillAUT(IAUTMainPO po) {
        AutDTO autDTO = new AutDTO();
        autDTO.setId(i2str(po.getId()));
        autDTO.setName(po.getName());
        autDTO.setAutToolkit(po.getToolkit());
        autDTO.setGenerateNames(po.isGenerateNames());

        ObjectMappingDTO omDTO = new ObjectMappingDTO();
        fillObjectMapping(omDTO, po.getObjMap());
        autDTO.setObjectMapping(omDTO);

        // Sort the list of AUT Configurations alphabetically by name
        List<IAUTConfigPO> sortedAutConfigs = 
            new ArrayList<IAUTConfigPO>(po.getAutConfigSet());
        Collections.sort(sortedAutConfigs, new Comparator<IAUTConfigPO>() {
            public int compare(IAUTConfigPO autConfig1, 
                    IAUTConfigPO autConfig2) {
                
                return autConfig1.getName().compareTo(autConfig2.getName());
            }
        });
        for (IAUTConfigPO conf : sortedAutConfigs) {
            fillAUTConfig(autDTO, conf);
        }

        for (String autId : po.getAutIds()) {
            autDTO.addAutId(autId);
        }
        m_projectDTO.addAut(autDTO);
    }
    
    /**
     * fill AUT configuration
     * @param autDTO will be contained the AUT config info
     * @param po AUT config object
     */
    private void fillAUTConfig(AutDTO autDTO, IAUTConfigPO po) {
        AutConfigDTO autConfDTO = new AutConfigDTO();
        autConfDTO.setName(po.getName());

        // Sort the list of configuration entries by key
        final List<String> sortedConfigKeys = 
            new ArrayList<String>(po.getAutConfigKeys());
        Collections.sort(sortedConfigKeys);

        for (String key : sortedConfigKeys) {
            MapEntryDTO mapEntryDTO = new MapEntryDTO();
            mapEntryDTO.setKey(key);
            mapEntryDTO.setValue(po.getValue(key, StringConstants.EMPTY));
            autConfDTO.addConfAttrMapEntry(mapEntryDTO);
        }
        autDTO.addConfig(autConfDTO);
    }

    /**
     * fill object mapping
     * @param omDTO will be contained the object mapping info
     * @param po object mapping object
     */
    private void fillObjectMapping(ObjectMappingDTO omDTO,
            IObjectMappingPO po) {
        IObjectMappingProfilePO profilePo = po.getProfile();
        ObjectMappingProfileDTO profileDTO = new ObjectMappingProfileDTO();
        
        profileDTO.setName(profilePo.getName());
        profileDTO.setContextFactor(profilePo.getContextFactor());
        profileDTO.setNameFactor(profilePo.getNameFactor());
        profileDTO.setPathFactor(profilePo.getPathFactor());
        profileDTO.setThreshold(profilePo.getThreshold());

        omDTO.setProfile(profileDTO);

        OmCategoryDTO mappedDTO = new OmCategoryDTO();
        fillObjectMappingCategory(mappedDTO, po.getMappedCategory());
        omDTO.setMapped(mappedDTO);
        OmCategoryDTO unmappedComponentDTO = new OmCategoryDTO();
        fillObjectMappingCategory(unmappedComponentDTO,
                po.getUnmappedLogicalCategory());
        omDTO.setUnmappedComponent(unmappedComponentDTO);
        OmCategoryDTO unmappedTechnicalDTO = new OmCategoryDTO();
        fillObjectMappingCategory(unmappedTechnicalDTO,
                po.getUnmappedTechnicalCategory());
        omDTO.setUnmappedTechnical(unmappedTechnicalDTO);
    }
    
    /**
     * fill object mapping category
     * @param categoryDTO will be contained the object mapping config info
     * @param category object mapping config object
     */
    private void fillObjectMappingCategory(OmCategoryDTO categoryDTO,
            IObjectMappingCategoryPO category) {
        categoryDTO.setName(category.getName());
        for (IObjectMappingCategoryPO subcategory : category
                .getUnmodifiableCategoryList()) {

            OmCategoryDTO mappedDTO = new OmCategoryDTO();
            fillObjectMappingCategory(mappedDTO, subcategory);
            categoryDTO.addCategories(mappedDTO);
        }
        for (IObjectMappingAssoziationPO assoc : category
                .getUnmodifiableAssociationList()) {

            OmEntryDTO assocDTO = new OmEntryDTO();
            fillObjectMappingAssociation(assocDTO, assoc);
            categoryDTO.addAssociation(assocDTO);
        }
    }

    /**
     * fill object mapping association
     * @param assocDTO will be contained the object mapping association info
     * @param assoc object mapping association object
     */
    private void fillObjectMappingAssociation(OmEntryDTO assocDTO,
            IObjectMappingAssoziationPO assoc) {
        final ICompIdentifierPO technicalName = assoc.getTechnicalName();
        // tecName == null means not mapped

        if (technicalName != null) {
            TechnicalNameDTO technicalNameDTO = new TechnicalNameDTO();
            fillTechnicalName(technicalNameDTO, technicalName);
            assocDTO.setTechnicalName(technicalNameDTO);
        }
        assocDTO.setType(assoc.getType());

        for (String logicalName : assoc.getLogicalNames()) {
            assocDTO.addLogicalName(logicalName);
        }
    }

    /**
     * fill technical name
     * @param tnameDTO will be contained the technical name info
     * @param po technical name object
     */
    private void fillTechnicalName(TechnicalNameDTO tnameDTO,
            ICompIdentifierPO po) {
        tnameDTO.setComponentClassName(po.getComponentClassName());
        tnameDTO.setSupportedClassName(po.getSupportedClassName());
        tnameDTO.setAlternativeDisplayName(po.getAlternativeDisplayName());

        IObjectMappingProfilePO profilePo = po.getProfilePO();
        if (profilePo != null) {
            ObjectMappingProfileDTO profileDto = new ObjectMappingProfileDTO();
            profileDto.setName(profilePo.getName());
            profileDto.setContextFactor(profilePo.getContextFactor());
            profileDto.setNameFactor(profilePo.getNameFactor());
            profileDto.setPathFactor(profilePo.getPathFactor());
            profileDto.setThreshold(profilePo.getThreshold());
            tnameDTO.setObjectMappingProfile(profileDto);
        }
        
        for (Object n : po.getNeighbours()) {
            tnameDTO.addNeighbour((String)n);
        }
        for (Object h : po.getHierarchyNames()) {
            tnameDTO.addHierarchyName((String)h);
        }
    }

    /** fill the spec persistent objects
     * @throws OperationCanceledException */
    private void handleSpecPersistables() throws OperationCanceledException {
        for (ISpecPersistable tcOrCat : m_project.getSpecObjCont()
                .getSpecObjList()) {
            
            checkCancel();
            if (tcOrCat instanceof ICategoryPO) {
                CategoryDTO catDTO = new CategoryDTO(tcOrCat);
                fillCategory(catDTO, (ICategoryPO)tcOrCat);
                m_projectDTO.addCategory(catDTO);
            } else {
                TestCaseDTO tcDTO = new TestCaseDTO(tcOrCat); 
                fillTestCase(tcDTO, (ISpecTestCasePO)tcOrCat);
                m_projectDTO.addCategory(tcDTO);
            }
        }
    }

    /**
     * fill category
     * @param categoryDTO will be contained the category info
     * @param po category object
     */
    private void fillCategory(CategoryDTO categoryDTO, ICategoryPO po) {
        for (Object o : po.getUnmodifiableNodeList()) {
            if (o instanceof ISpecPersistable) {
                ISpecPersistable tcOrCat = (ISpecPersistable)o;
                if (tcOrCat instanceof ICategoryPO) {
                    CategoryDTO catDTO = new CategoryDTO(tcOrCat);
                    fillCategory(catDTO, (ICategoryPO)tcOrCat);
                    categoryDTO.addNode(catDTO);
                } else {
                    TestCaseDTO tcDTO = new TestCaseDTO(tcOrCat); 
                    fillTestCase(tcDTO, (ISpecTestCasePO)tcOrCat);
                    categoryDTO.addNode(tcDTO);
                }
            }
        }
    }

    /**
     * fill test cases
     * @param tcDTO will be contained the test case info
     * @param po spec test case object
     */
    private void fillTestCase(TestCaseDTO tcDTO, ISpecTestCasePO po) {
        for (Object o : po.getUnmodifiableNodeList()) {
            if (o instanceof ICapPO) {
                ICapPO capPO = (ICapPO)o;
                CapDTO capDTO = new CapDTO(capPO);
                fillCap(capDTO, capPO);
                tcDTO.addTestStep(capDTO);
            } else if (o instanceof IExecTestCasePO) {
                IExecTestCasePO tcPO = (IExecTestCasePO)o;
                RefTestCaseDTO refTestCaseDTO = new RefTestCaseDTO(tcPO);
                fillRefTestCase(refTestCaseDTO, tcPO);
                tcDTO.addTestStep(refTestCaseDTO);
            }
        }

        for (IParamDescriptionPO paramPO : po.getParameterList()) {
            ParamDescriptionDTO pdDTO = new ParamDescriptionDTO();
            fillParamDescription(pdDTO, paramPO);
            tcDTO.addParameterDescription(pdDTO);
        }

        tcDTO.setInterfaceLocked(po.isInterfaceLocked());

        tcDTO.setDatafile(po.getDataFile());
        if (po.getReferencedDataCube() != null) {
            tcDTO.setReferencedTestData(po.getReferencedDataCube().getName());
        }
        final ITDManager dataManager = po.getDataManager();
        if (dataManager != null) {
            if (po.getReferencedDataCube() == null) {
                TDManagerDTO tdmDTO = new TDManagerDTO();
                fillTDManager(tdmDTO, dataManager);
                tcDTO.setTDManager(tdmDTO);
            }
        }

        for (Object o : po.getEventExecTcMap().keySet()) {
            IEventExecTestCasePO evTc = po.getEventExecTC((String)o);
            EventTestCaseDTO etcDTO = new EventTestCaseDTO(evTc);
            fillEventTestCase(etcDTO, evTc);
            tcDTO.addEventTestcase(etcDTO);
        }
    }

    /**
     * fill cap
     * @param capDTO will be contained the cap info
     * @param po cap object
     */
    private void fillCap(CapDTO capDTO, ICapPO po) {
        capDTO.setActionName(po.getActionName());
        capDTO.setComponentName(po.getComponentName());
        capDTO.setComponentType(po.getComponentType());
        capDTO.setDatafile(po.getDataFile());

        TDManagerDTO tdmDTO = new TDManagerDTO();
        fillTDManager(tdmDTO, po.getDataManager());
        capDTO.setTDManager(tdmDTO);
        for (IParamDescriptionPO desc : po.getParameterList()) {
            ParamDescriptionDTO pdDTO = new ParamDescriptionDTO();
            fillParamDescription(pdDTO, desc);
            capDTO.addParameterDescription(pdDTO);
        }
    }

    /**
     * fill references test cases
     * @param rtcDTO will be contained the references test cases info
     * @param po references test cases object
     */
    private void fillRefTestCase(RefTestCaseDTO rtcDTO, IExecTestCasePO po) {
        String execName = po.getRealName();
        if (execName == null) {
            execName = StringConstants.EMPTY;
        }

        if (po.getSpecTestCase() != null) {
            String specName = po.getSpecTestCase().getName();
            if (execName.equals(specName)) {
                rtcDTO.setName(null);
            } else {
                rtcDTO.setName(execName);
            }
        } else {
            rtcDTO.setName(execName);
        }
        
        rtcDTO.setTestcaseGuid(po.getSpecTestCaseGuid());

        // A Project GUID value of null indicates that the Test Case Reference
        // and the referenced Test Case are in the same Project. If they are
        // *not* in the same Project, then the exported file needs to contain
        // information about the Reused Project (i.e. Project GUID).
        if (po.getProjectGuid() != null) {
            rtcDTO.setProjectGuid(po.getProjectGuid());
        }
        rtcDTO.setHasOwnTestdata(!po.getHasReferencedTD());
        rtcDTO.setDatafile(po.getDataFile());
        if (po.getReferencedDataCube() != null) {
            rtcDTO.setReferencedTestData(po.getReferencedDataCube().getName());
        }
        
        if (!po.getHasReferencedTD()) {
            // ExecTestCasePO doesn't have an own parameter list.
            // It uses generally the parameter from the associated
            // SpecTestCase.
            final ITDManager dataManager = po.getDataManager();
            if (dataManager != null) {
                if (po.getReferencedDataCube() == null) {
                    TDManagerDTO tdmDTO = new TDManagerDTO();
                    fillTDManager(tdmDTO, dataManager);
                    rtcDTO.setTDManager(tdmDTO);
                }
            }
        }

        for (ICompNamesPairPO name : po.getCompNamesPairs()) {
            CompNamesDTO compNameDTO = new CompNamesDTO();
            compNameDTO.setOriginalName(name.getFirstName());
            compNameDTO.setNewName(name.getSecondName());
            compNameDTO.setPropagated(name.isPropagated());
            rtcDTO.addOverriddenNames(compNameDTO);
        }
    }

    /**
     * fill event test cases
     * @param evDTO will be contained the event test cases info
     * @param po event test cases object
     */
    private void fillEventTestCase(EventTestCaseDTO evDTO,
            IEventExecTestCasePO po) {
        fillRefTestCase(evDTO, po);
        evDTO.setEventType(po.getEventType());
        ReentryProperty.Enum reentryProperty = ReentryProperty.Enum.forInt(po
                .getReentryProp().getValue());
        evDTO.setReentryProperty(reentryProperty.toString());
        if (reentryProperty == ReentryProperty.RETRY) {
            Integer maxRetries = po.getMaxRetries();
            if (maxRetries != null) {
                evDTO.setMaxRetries(maxRetries);
            }
        }
    }

    /** fill Exec persistables 
     * @throws OperationCanceledException */
    private void handleExecPersistables() throws OperationCanceledException {
        for (IExecPersistable tsOrTjOrCat : m_project.getExecObjCont()
                .getExecObjList()) {

            checkCancel();
            if (tsOrTjOrCat instanceof ICategoryPO) {
                ExecCategoryDTO eCatDTO = new ExecCategoryDTO(tsOrTjOrCat);
                fillCategory(eCatDTO, (ICategoryPO) tsOrTjOrCat);
                m_projectDTO.addExecCategorie(eCatDTO);
            } else if (tsOrTjOrCat instanceof ITestSuitePO) {
                TestSuiteDTO tsDTO = new TestSuiteDTO(tsOrTjOrCat);
                fillTestsuite(tsDTO, (ITestSuitePO) tsOrTjOrCat);
                m_projectDTO.addExecCategorie(tsDTO);
            } else {
                TestJobDTO tjDTO = new TestJobDTO(tsOrTjOrCat);
                fillTestJob(tjDTO, (ITestJobPO) tsOrTjOrCat);
                m_projectDTO.addExecCategorie(tjDTO);
            }
        }
    }

    /**
     * fill category
     * @param execCatDTO will be contained the categories info
     * @param po category object
     */
    private void fillCategory(ExecCategoryDTO execCatDTO, ICategoryPO po) {
        for (Object o : po.getUnmodifiableNodeList()) {
            if (o instanceof IExecPersistable) {
                IExecPersistable tcOrCat = (IExecPersistable)o;
                
                if (tcOrCat instanceof ICategoryPO) {
                    ExecCategoryDTO eCatDTO = new ExecCategoryDTO(tcOrCat);
                    fillCategory(eCatDTO, (ICategoryPO) tcOrCat);
                    execCatDTO.addNode(eCatDTO);
                } else if (tcOrCat instanceof ITestSuitePO) {
                    TestSuiteDTO tsDTO = new TestSuiteDTO(tcOrCat);
                    fillTestsuite(tsDTO, (ITestSuitePO) tcOrCat);
                    execCatDTO.addNode(tsDTO);
                } else {
                    TestJobDTO tjDTO = new TestJobDTO(tcOrCat);
                    fillTestJob(tjDTO, (ITestJobPO) tcOrCat);
                    execCatDTO.addNode(tjDTO);
                }
            }
        }
    }

    /**
     * fill test suites
     * @param tsDTO will be contained the test suites info
     * @param po test suite object
     */
    private void fillTestsuite(TestSuiteDTO tsDTO, ITestSuitePO po) {
        if (po.getAut() != null) {
            tsDTO.setSelectedAut(i2str(po.getAut().getId()));
        } else {
            tsDTO.setSelectedAut(null);
        }
        tsDTO.setStepDelay(po.getStepDelay());
        tsDTO.setRelevant(po.getRelevant());
        tsDTO.setCommandLineParameter(po.getCmdLineParameter());

        for (Object o : po.getUnmodifiableNodeList()) {
            if (o instanceof IExecTestCasePO) {
                IExecTestCasePO tc = (IExecTestCasePO)o;
                RefTestCaseDTO rtcDTO = new RefTestCaseDTO(tc);
                fillRefTestCase(rtcDTO, tc);
                tsDTO.addUsedTestcase(rtcDTO);
            }
        }
        for (Object o : po.getDefaultEventHandler().keySet()) {
            String eventType = (String)o;
            Integer evProp = po.getDefaultEventHandler().get(eventType);
            ReentryProperty.Enum reentryProperty = ReentryProperty.Enum
                    .forInt(evProp);
            EventHandlerDTO ehDTO = new EventHandlerDTO();
            ehDTO.setEvent(eventType);
            ehDTO.setReentryProperty(reentryProperty.toString());

            // Trac#1908
            // since EventHandler on TestSuites are fakes, we can not
            // use the real data. The default for this is set to 1.
            if (reentryProperty == ReentryProperty.RETRY) {
                ehDTO.setMaxRetries(1);
            }
            tsDTO.addEventHandler(ehDTO);
        }
    }

    /**
     * fill test jobs
     * @param tjDTO will be contained the test jobs info
     * @param tj test job object
     */
    private void fillTestJob(TestJobDTO tjDTO, ITestJobPO tj) {
        for (Object child : tj.getUnmodifiableNodeList()) {
            if (child instanceof IRefTestSuitePO) {
                IRefTestSuitePO rts = (IRefTestSuitePO)child;
                RefTestSuiteDTO rtsDTO = new RefTestSuiteDTO(rts);
                rtsDTO.setName(rts.getRealName());
                rtsDTO.setGuid(rts.getGuid());
                rtsDTO.setTsGuid(rts.getTestSuiteGuid());
                rtsDTO.setAutId(rts.getTestSuiteAutID());
                tjDTO.addRefTestSuite(rtsDTO);
            }
        }
    }

    /** fill reused projects */
    private void handleReusedProjects() {
        for (IReusedProjectPO reusedProject : m_project.getUsedProjects()) {
            ReusedProjectDTO rpDTO = new ReusedProjectDTO(); 
            fillReusedProject(rpDTO, reusedProject);
            m_projectDTO.addReusedProject(rpDTO);
        }
        m_monitor.worked(1);
    }

    /**
     * fill reused project
     * @param rpDTO will be contained the reused project info
     * @param po reused project object
     */
    private void fillReusedProject(ReusedProjectDTO rpDTO,
            IReusedProjectPO po) {
        rpDTO.setProjectName(ProjectNameBP.getInstance().getName(
                po.getProjectGuid()));
        rpDTO.setProjectGUID(po.getProjectGuid());
        if (po.getMajorNumber() != null) {
            rpDTO.setMajorProjectVersion(po.getMajorNumber());
        }
        if (po.getMinorNumber() != null) {
            rpDTO.setMinorProjectVersion(po.getMinorNumber());
        }
        if (po.getMicroNumber() != null) {
            rpDTO.setMicroProjectVersion(po.getMicroNumber());
        }
        rpDTO.setProjectVersionQualifier(po.getVersionQualifier());
    }

    /**
     * return with a test result summary dto list
     * @param poSummaryList the list of original test result summary objects
     * @return test result summary dto list
     */
    private List<TestresultSummaryDTO> getTestResultSummary(
            List<ITestResultSummaryPO> poSummaryList) {

        List<TestresultSummaryDTO> resultDTO =
                new ArrayList<TestresultSummaryDTO>();

        for (ITestResultSummaryPO poSummary : poSummaryList) {
            
            if (!poSummary.isTestsuiteRelevant()) {
                continue;
            }
            TestresultSummaryDTO trsDTO = new TestresultSummaryDTO(poSummary);
            if (AlmReportStatus.NOT_YET_REPORTED.equals(poSummary
                .getAlmReportStatus())) {
                trsDTO.setAlmStatus(AlmReportStatus.REPORT_DISCARDED);
            }
            Map<String, IMonitoringValue> 
                    tmpMap = poSummary.getMonitoringValues();
            Iterator it = tmpMap.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry pairs = (Map.Entry) it.next();
                MonitoringValue tmp = (MonitoringValue)pairs.getValue();
                MonitoringValuesDTO monValDTO = new MonitoringValuesDTO();  
                monValDTO.setKey((String)pairs.getKey());
                monValDTO.setCategory(tmp.getCategory());
                monValDTO.setSignificant(tmp.isSignificant());
                monValDTO.setType(tmp.getType());
                monValDTO.setValue(tmp.getValue());
                trsDTO.addMonitoringValue(monValDTO);
            }
            resultDTO.add(trsDTO);
        }

        return resultDTO;
    }
    
    /** set project versions */
    private void setProjectVersions() {
        m_projectDTO.setMetaDataVersion(m_project.getClientMetaDataVersion());
        if (m_project.getMajorProjectVersion() != null) {
            m_projectDTO.setMajorProjectVersion(
                    m_project.getMajorProjectVersion());
        }
        if (m_project.getMinorProjectVersion() != null) {
            m_projectDTO.setMinorProjectVersion(
                    m_project.getMinorProjectVersion());
        }
        if (m_project.getMicroProjectVersion() != null) {
            m_projectDTO.setMicroProjectVersion(
                    m_project.getMicroProjectVersion());
        }
        m_projectDTO.setProjectVersionQualifier(
                m_project.getProjectVersionQualifier());
    }
    
    /**
     * fill project ALMs
     * @param projectProperties project properties
     * @throws OperationCanceledException 
     */
    private void fillALM(IProjectPropertiesPO projectProperties)
            throws OperationCanceledException {
        checkCancel();
        m_projectDTO.setAlmRepositoryName(
                projectProperties.getALMRepositoryName());
        
        for (IALMReportingRulePO rule : projectProperties
                .getALMReportingRules()) {
            ReportingRuleDTO ruleDTO = new ReportingRuleDTO();
            fillReportingRule(ruleDTO, rule);
            m_projectDTO.addReportingRule(ruleDTO);
        }
    }

    /**
     * fill reporting rule dto
     * @param ruleDTO will contained the reporting rules info
     * @param po the original reporting rule object
     */
    private void fillReportingRule(ReportingRuleDTO ruleDTO,
            IALMReportingRulePO po) {
        ruleDTO.setName(po.getName());
        ruleDTO.setFieldID(po.getAttributeID());
        ruleDTO.setValue(po.getValue());
        ruleDTO.setType(po.getType().toString());            
    }

    /**
     * fill tracking config
     * @param projectProperties will contained the project properties
     */
    private void fillTrackingConfig(IProjectPropertiesPO projectProperties) {
        m_projectDTO.setTrackingEnabled(projectProperties
                .getIsTrackingActivated());
        m_projectDTO.setTrackingAttribute(projectProperties
                .getTrackChangesSignature());
        TrackingUnit trackChangesUnit = projectProperties.getTrackChangesUnit();
        if (trackChangesUnit != null) {
            m_projectDTO.setTrackingUnit(trackChangesUnit.toString());
        }
        Integer trackChangesSpan = projectProperties.getTrackChangesSpan();
        if (trackChangesSpan != null) {
            m_projectDTO.setTrackingSpan(trackChangesSpan);
        }
    }
    
    /**
     * Sorts (by GUID) a copy of the AUT list of the given Project and returns 
     * that sorted copy.
     * 
     * @param po The Project containing the list of AUTs to sort.
     * @return the sorted copy.
     */
    private List<IAUTMainPO> getSortedAutList(IProjectPO po) {
        List<IAUTMainPO> sortedAuts = 
            new ArrayList<IAUTMainPO>(po.getAutMainList());
        Collections.sort(sortedAuts, new Comparator<IAUTMainPO>() {
            public int compare(IAUTMainPO aut1, IAUTMainPO aut2) {
                return aut1.getGuid().compareTo(aut2.getGuid());
            }
        });
        return sortedAuts;
    }
    
    /**
     * @param l long number
     * @return string of l or null if l is null
     */
    private String i2str(Long l) {
        if (l != null) {
            return l.toString();
        }
        return StringConstants.EMPTY;
    }
    
    /**
     * @param project The project for which the work is predicted.
     * @param includeTestResultSummaries true if the project contains test result summaries
     * @return The predicted amount of work required to save a project.
     */
    public static int getPredictedWork(IProjectPO project,
            boolean includeTestResultSummaries) {
        int work = 0;

        // (start of fillProject = 1)
        work++;

        // (nodes = 1)
        work++;

        // (component names = 1)
        work++;
        
        if (includeTestResultSummaries) {
            try {
                work += TestResultSummaryPM.countOfTestResultSummaries(
                        project, null);
            } catch (PMException e) {
                // nothing here
            }
        }

        // (AUT=1)
        work++; 
        
        // (reused project=1)
        work++; 

        // (end of fillProject = 1))
        work++;

        return work;
    }
    
    /**
     * Checks whether the operation has been canceled. If the operation has been
     * canceled, an <code>OperationCanceledException</code> will be thrown.
     * 
     * @throws OperationCanceledException if the operation has been canceled.
     */
    private void checkCancel() throws OperationCanceledException {
        if (m_monitor.isCanceled()) {
            throw new OperationCanceledException();
        }
    }
}