/***
 * Copyright (c) 2009-2019 Jean-François Lamy
 *
 * Licensed under the Non-Profit Open Software License version 3.0  ("Non-Profit OSL" 3.0)
 * License text at https://github.com/jflamy/owlcms4/blob/master/LICENSE.txt
 */
package app.owlcms.data.competition;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.TreeSet;

import javax.persistence.Cacheable;
import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Transient;

import org.slf4j.LoggerFactory;

import app.owlcms.Main;
import app.owlcms.data.athlete.Athlete;
import app.owlcms.data.athlete.AthleteRepository;
import app.owlcms.data.athlete.Gender;
import app.owlcms.data.athleteSort.AthleteSorter;
import app.owlcms.data.athleteSort.AthleteSorter.Ranking;
import app.owlcms.data.jpa.LocaleAttributeConverter;
import app.owlcms.ui.results.Resource;
import app.owlcms.utils.ResourceWalker;
import ch.qos.logback.classic.Logger;

/**
 * Class Competition.
 */
@Cacheable
@Entity
public class Competition {
    public static final String DEFAULT_PROTOCOL_NAME = "Protocol_en.xls";
    public static final String DEFAULT_PACKAGE_NAME = "Total_en.xls";

    final static private Logger logger = (Logger) LoggerFactory.getLogger(Competition.class);

    private static Competition competition;

    /**
     * Gets the current.
     *
     * @return the current
     */
    public static Competition getCurrent() {
        if (competition == null) {
            competition = CompetitionRepository.findAll().get(0);
        }
        return competition;
    }

    public static void setCurrent(Competition c) {
        competition = c;
    }

    public static void splitByGender(List<Athlete> sortedAthletes, List<Athlete> sortedMen, List<Athlete> sortedWomen) {
        for (Athlete l : sortedAthletes) {
            Gender gender = l.getGender();
            if (Gender.M == gender) {
                sortedMen.add(l);
            } else if (Gender.F == gender) {
                sortedWomen.add(l);
            } else {
                throw new RuntimeException("gender is " + gender);
            }
        }
    }

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    Long id;
    private String competitionName;
    private LocalDate competitionDate = null;
    private String competitionOrganizer;
    private String competitionSite;

    private String competitionCity;
    private String federation;
    private String federationAddress;
    private String federationEMail;

    private String federationWebSite;

    @Convert(converter = LocaleAttributeConverter.class)
    private Locale defaultLocale = null;
    private String protocolFileName;

    @Lob
    private byte[] protocolTemplate;
    private String finalPackageTemplateFileName;

    private String ageGroupsFileName;

    @Lob
    private byte[] finalPackageTemplate;

    private boolean enforce20kgRule;
    private boolean masters;
    /**
     * Add W75 and W80+ masters categories
     */
    @Column(columnDefinition = "boolean default false")
    private boolean mastersGenderEquality = false;

    /**
     * Do not require month and day for birth.
     */
    @Column(columnDefinition = "boolean default true")
    private boolean useBirthYear = true;

    /**
     * Idiosyncratic rule in Québec federation computes best lifter using Sinclair at bodyweight boundary.
     */
    @Column(columnDefinition = "boolean default false")
    private boolean useCategorySinclair = false;

    /**
     * For traditional competitions that have lower body weight comes out first. Tie breaker for identical Sinclair.
     */
    @Column(columnDefinition = "boolean default false")
    private boolean useOldBodyWeightTieBreak = false;

    /**
     * Obsolete. We no longer infer categories.
     */
    @Column(columnDefinition = "boolean default false")
    private boolean useRegistrationCategory = false;

    @Transient
    private HashMap<String, Object> reportingBeans;

    public void computeGlobalRankings() {
        this.setGlobalRankingRecompute(true);
        computeGlobalRankings(reportingBeans);
    }

    public void computeGlobalRankings(HashMap<String, Object> reportingBeans2) {
        List<Athlete> athletes = AthleteRepository.findAllByGroupAndWeighIn(null, true);
        if (athletes.isEmpty()) {
            // prevent outputting silliness.
            //throw new RuntimeException("No athletes.");
            return;
        }
        // extract club lists
        TreeSet<String> teams = new TreeSet<>();
        for (Athlete curAthlete : athletes) {
            teams.add(curAthlete.getTeam());
        }
        reportingBeans2.put("clubs", teams);
        logger.debug("teams {}", teams);

        List<Athlete> sortedAthletes;
        List<Athlete> sortedMen = null;
        List<Athlete> sortedWomen = null;

        sortedAthletes = AthleteSorter.resultsOrderCopy(athletes, Ranking.SNATCH);
        AthleteSorter.assignCategoryRanks(sortedAthletes, Ranking.SNATCH);
        sortedMen = new ArrayList<>(sortedAthletes.size());
        sortedWomen = new ArrayList<>(sortedAthletes.size());
        splitByGender(sortedAthletes, sortedMen, sortedWomen);
        reportingBeans2.put("mSn", sortedMen);
        reportingBeans2.put("wSn", sortedWomen);

        // only needed once
        reportingBeans2.put("nbMen", sortedMen.size());
        reportingBeans2.put("nbWomen", sortedWomen.size());
        reportingBeans2.put("nbAthletes", sortedAthletes.size());
        reportingBeans2.put("nbClubs", teams.size());
        if (sortedMen.size() > 0) {
            reportingBeans2.put("mClubs", teams);
        } else {
            reportingBeans2.put("mClubs", new ArrayList<String>());
        }
        if (sortedWomen.size() > 0) {
            reportingBeans2.put("wClubs", teams);
        } else {
            reportingBeans2.put("wClubs", new ArrayList<String>());
        }

        sortedAthletes = AthleteSorter.resultsOrderCopy(athletes, Ranking.CLEANJERK);
        AthleteSorter.assignCategoryRanks(sortedAthletes, Ranking.CLEANJERK);
        sortedMen = new ArrayList<>(sortedAthletes.size());
        sortedWomen = new ArrayList<>(sortedAthletes.size());
        splitByGender(sortedAthletes, sortedMen, sortedWomen);
        reportingBeans2.put("mCJ", sortedMen);
        reportingBeans2.put("wCJ", sortedWomen);

        sortedAthletes = AthleteSorter.resultsOrderCopy(athletes, Ranking.TOTAL);
        AthleteSorter.assignCategoryRanks(sortedAthletes, Ranking.TOTAL);
        sortedMen = new ArrayList<>(sortedAthletes.size());
        sortedWomen = new ArrayList<>(sortedAthletes.size());
        splitByGender(sortedAthletes, sortedMen, sortedWomen);
        reportingBeans2.put("mTot", sortedMen);
        reportingBeans2.put("wTot", sortedWomen);

        sortedAthletes = AthleteSorter.resultsOrderCopy(athletes, Ranking.SINCLAIR);
        AthleteSorter.assignSinclairRanksAndPoints(sortedAthletes, Ranking.SINCLAIR);
        sortedMen = new ArrayList<>(sortedAthletes.size());
        sortedWomen = new ArrayList<>(sortedAthletes.size());
        splitByGender(sortedAthletes, sortedMen, sortedWomen);
        reportingBeans2.put("mSinclair", sortedMen);
        reportingBeans2.put("wSinclair", sortedWomen);
        logger.debug("mSinclair {}", sortedMen);
        logger.debug("wSinclair {}", sortedWomen);

        sortedAthletes = AthleteSorter.resultsOrderCopy(athletes, Ranking.ROBI);
        AthleteSorter.assignSinclairRanksAndPoints(sortedAthletes, Ranking.ROBI);
        sortedMen = new ArrayList<>(sortedAthletes.size());
        sortedWomen = new ArrayList<>(sortedAthletes.size());
        splitByGender(sortedAthletes, sortedMen, sortedWomen);
        reportingBeans2.put("mRobi", sortedMen);
        reportingBeans2.put("wRobi", sortedWomen);

        sortedAthletes = AthleteSorter.resultsOrderCopy(athletes, Ranking.CUSTOM);
        AthleteSorter.assignCategoryRanks(sortedAthletes, Ranking.CUSTOM);
        sortedMen = new ArrayList<>(sortedAthletes.size());
        sortedWomen = new ArrayList<>(sortedAthletes.size());
        splitByGender(sortedAthletes, sortedMen, sortedWomen);
        reportingBeans2.put("mCus", sortedMen);
        reportingBeans2.put("wCus", sortedWomen);

        // team-oriented rankings. These put all the athletes from the same team
        // together,
        // sorted from best to worst, so that the top "n" can be given points
        sortedAthletes = AthleteSorter.teamRankingOrderCopy(athletes, Ranking.CUSTOM);
        sortedMen = new ArrayList<>(sortedAthletes.size());
        sortedWomen = new ArrayList<>(sortedAthletes.size());
        splitByGender(sortedAthletes, sortedMen, sortedWomen);
        reportingBeans2.put("mCustom", sortedMen);
        reportingBeans2.put("wCustom", sortedWomen);

        sortedAthletes = AthleteSorter.teamRankingOrderCopy(athletes, Ranking.COMBINED);
        sortedMen = new ArrayList<>(sortedAthletes.size());
        sortedWomen = new ArrayList<>(sortedAthletes.size());
        splitByGender(sortedAthletes, sortedMen, sortedWomen);
        reportingBeans2.put("mCombined", sortedMen);
        reportingBeans2.put("wCombined", sortedWomen);
        reportingBeans2.put("mwCombined", sortedAthletes);

        AthleteSorter.teamRankingOrder(sortedAthletes, Ranking.TOTAL);
        sortedMen = new ArrayList<>(sortedAthletes.size());
        sortedWomen = new ArrayList<>(sortedAthletes.size());
        splitByGender(sortedAthletes, sortedMen, sortedWomen);
        reportingBeans2.put("mTeam", sortedMen);
        reportingBeans2.put("wTeam", sortedWomen);
        reportingBeans2.put("mwTeam", sortedAthletes);
    }

    public String getAgeGroupsFileName() {
        return ageGroupsFileName;
    }

    /**
     * Gets the competition city.
     *
     * @return the competition city
     */
    public String getCompetitionCity() {
        return competitionCity;
    }

    /**
     * Gets the competition date.
     *
     * @return the competition date
     */
    public LocalDate getCompetitionDate() {
        return competitionDate;
    }

    /**
     * Gets the competition name.
     *
     * @return the competition name
     */
    public String getCompetitionName() {
        return competitionName;
    }

    /**
     * Gets the competition organizer.
     *
     * @return the competition organizer
     */
    public String getCompetitionOrganizer() {
        return competitionOrganizer;
    }

    /**
     * Gets the competition site.
     *
     * @return the competition site
     */
    public String getCompetitionSite() {
        return competitionSite;
    }

    /**
     * Gets the default locale.
     *
     * @return the default locale
     */
    public Locale getDefaultLocale() {
        return defaultLocale;
    }

    /**
     * Gets the federation.
     *
     * @return the federation
     */
    public String getFederation() {
        return federation;
    }

    /**
     * Gets the federation address.
     *
     * @return the federation address
     */
    public String getFederationAddress() {
        return federationAddress;
    }

    /**
     * Gets the federation E mail.
     *
     * @return the federation E mail
     */
    public String getFederationEMail() {
        return federationEMail;
    }

    /**
     * Gets the federation web site.
     *
     * @return the federation web site
     */
    public String getFederationWebSite() {
        return federationWebSite;
    }

    public byte[] getFinalPackageTemplate() {
        return finalPackageTemplate;
    }

    /**
     * Gets the result template file name.
     *
     * @return the result template file name
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public String getFinalPackageTemplateFileName() throws IOException {
        if (finalPackageTemplateFileName == null) {
            List<Resource> resourceList = new ResourceWalker().getResourceList("/templates/competitionBook",
                    ResourceWalker::relativeName,  null);
            for (Resource r : resourceList) {
                if (this.isMasters() && r.getFileName().startsWith("Masters_")) {
                    return r.getFileName();
                } else if (r.getFileName().startsWith("Total_")) {
                    return r.getFileName();
                }
            }
            return null;
        } else {
            return finalPackageTemplateFileName;
        }
    }

    @SuppressWarnings("unchecked")
    public List<Athlete> getGlobalSinclairRanking(Gender gender) {
        return (List<Athlete>) reportingBeans.get(gender == Gender.F ? "wSinclair" : "mSinclair");
    }

    /**
     * Gets the id.
     *
     * @return the id
     */
    public Long getId() {
        return id;
    }

    /**
     * Gets the invited if born before.
     *
     * @return the invited if born before
     */
    public Integer getInvitedIfBornBefore() {
        return 0;
    }

    /**
     * Gets the locale.
     *
     * @return the locale
     */
    public Locale getLocale() {
        return getDefaultLocale();
    }

    /**
     * Gets the masters.
     *
     * @return the masters
     */
    public boolean getMasters() {
        return isMasters();
    }

    /**
     * Gets the protocol file name.
     *
     * @return the protocol file name
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public String getProtocolFileName() throws IOException {
        if (protocolFileName == null) {
            List<Resource> resourceList = new ResourceWalker().getResourceList("/templates/protocol",
                    ResourceWalker::relativeName, null);
            for (Resource r : resourceList) {
                if (this.isMasters() && r.getFileName().startsWith("Masters_")) {
                    return r.getFileName();
                } else if (r.getFileName().startsWith("Protocol_")) {
                    return r.getFileName();
                }
            }
            return null;
        } else {
            return protocolFileName;
        }
    }

    public byte[] getProtocolTemplate() {
        return protocolTemplate;
    }

    /**
     * Checks if is enforce 20 kg rule.
     *
     * @return true, if is enforce 20 kg rule
     */
    public boolean isEnforce20kgRule() {
        return enforce20kgRule;
    }

    public boolean isGenderOrder() {
        return Main.getBooleanParam("genderOrder");
    }

    /**
     * @return the globalRankingRecompute
     */
    public boolean isGlobalRankingRecompute() {
        return this.reportingBeans != null;
    }

    /**
     * Checks if is masters.
     *
     * @return true, if is masters
     */
    public boolean isMasters() {
        return masters;
    }

    public boolean isMastersGenderEquality() {
        return mastersGenderEquality;
    }

    /**
     * Checks if is use birth year.
     *
     * @return the useBirthYear
     */
    public boolean isUseBirthYear() {
        return useBirthYear;
    }

    /**
     * Checks if is use category sinclair.
     *
     * @return true, if is use category sinclair
     */
    public boolean isUseCategorySinclair() {
        return useCategorySinclair;
    }

    /**
     * Checks if is use old body weight tie break.
     *
     * @return true, if is use old body weight tie break
     */
    public boolean isUseOldBodyWeightTieBreak() {
        return useOldBodyWeightTieBreak;
    }

    /**
     * Checks if is use registration category.
     *
     * @return true, if is use registration category
     */
    @Deprecated
    public boolean isUseRegistrationCategory() {
        return false;
    }

    public void setAgeGroupsFileName(String localizedName) {
        this.ageGroupsFileName = localizedName;
    }

    /**
     * Sets the competition city.
     *
     * @param competitionCity the new competition city
     */
    public void setCompetitionCity(String competitionCity) {
        this.competitionCity = competitionCity;
    }

    /**
     * Sets the competition date.
     *
     * @param localDate the new competition date
     */
    public void setCompetitionDate(LocalDate localDate) {
        this.competitionDate = localDate;
    }

    /**
     * Sets the competition name.
     *
     * @param competitionName the new competition name
     */
    public void setCompetitionName(String competitionName) {
        this.competitionName = competitionName;
    }

    /**
     * Sets the competition organizer.
     *
     * @param competitionOrganizer the new competition organizer
     */
    public void setCompetitionOrganizer(String competitionOrganizer) {
        this.competitionOrganizer = competitionOrganizer;
    }

    /**
     * Sets the competition site.
     *
     * @param competitionSite the new competition site
     */
    public void setCompetitionSite(String competitionSite) {
        this.competitionSite = competitionSite;
    }

    public void setDefaultLocale(Locale defaultLocale) {
        this.defaultLocale = defaultLocale;
    }

    public void setEnforce20kgRule(boolean enforce20kgRule) {
        this.enforce20kgRule = enforce20kgRule;
    }

    /**
     * Sets the federation.
     *
     * @param federation the new federation
     */
    public void setFederation(String federation) {
        this.federation = federation;
    }

    /**
     * Sets the federation address.
     *
     * @param federationAddress the new federation address
     */
    public void setFederationAddress(String federationAddress) {
        this.federationAddress = federationAddress;
    }

    /**
     * Sets the federation E mail.
     *
     * @param federationEMail the new federation E mail
     */
    public void setFederationEMail(String federationEMail) {
        this.federationEMail = federationEMail;
    }

    /**
     * Sets the federation web site.
     *
     * @param federationWebSite the new federation web site
     */
    public void setFederationWebSite(String federationWebSite) {
        this.federationWebSite = federationWebSite;
    }

    public void setFinalPackageTemplate(byte[] finalPackageTemplate) {
        this.finalPackageTemplate = finalPackageTemplate;
    }

    /**
     * Sets the result template file name.
     *
     * @param finalPackageTemplateFileName the new result template file name
     */
    public void setFinalPackageTemplateFileName(String finalPackageTemplateFileName) {
        this.finalPackageTemplateFileName = finalPackageTemplateFileName;
    }

    /**
     * @param globalRankingRecompute the globalRankingRecompute to set
     */
    public void setGlobalRankingRecompute(boolean globalRankingRecompute) {
        if (globalRankingRecompute) {
            this.reportingBeans = new HashMap<>();
        } else {
            this.reportingBeans = null;
        }
    }

    /**
     * Sets the invited if born before.
     *
     * @param invitedIfBornBefore the new invited if born before
     */
    public void setInvitedIfBornBefore(Integer invitedIfBornBefore) {
    }

    public void setMasters(boolean masters) {
        this.masters = masters;
    }

    public void setMastersGenderEquality(boolean mastersGenderEquality) {
        this.mastersGenderEquality = mastersGenderEquality;
    }

    /**
     * Sets the protocol file name.
     *
     * @param protocolFileName the new protocol file name
     */
    public void setProtocolFileName(String protocolFileName) {
        this.protocolFileName = protocolFileName;
    }

    public void setProtocolTemplate(byte[] protocolTemplate) {
        this.protocolTemplate = protocolTemplate;
    }

    /**
     * Sets the use birth year.
     *
     * @param b the new use birth year
     */
    public void setUseBirthYear(boolean b) {
        this.useBirthYear = b;
    }

    /**
     * Sets the use registration category. No longer used. We always use the category. Only kept for backward
     * compatibility.
     *
     * @param useRegistrationCategory the useRegistrationCategory to set
     */
    @Deprecated
    public void setUseRegistrationCategory(boolean useRegistrationCategory) {
        this.useRegistrationCategory = false;
    }
}
