/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package fr.insalyon.creatis.vip.physicalproperties.server.dao;

import fr.insalyon.creatis.vip.physicalproperties.client.bean.ChemicalBlend;
import fr.insalyon.creatis.vip.physicalproperties.client.bean.Distribution;
import fr.insalyon.creatis.vip.physicalproperties.client.bean.DistributionInstance;
import fr.insalyon.creatis.vip.physicalproperties.client.bean.Echogenicity;
import fr.insalyon.creatis.vip.physicalproperties.client.bean.MagneticProperty;
import fr.insalyon.creatis.vip.physicalproperties.client.bean.PhysicalProperty;
import fr.insalyon.creatis.vip.physicalproperties.client.bean.Tissue;
import java.util.List;

/**
 *
 * @author glatard
 */
public interface TissueDAO {

    //tissues
    public List<Tissue> getTissues();
    public void addTissue(Tissue t);
    public void updateTissue(Tissue t);
    public void deleteTissue(Tissue t);

    //physical properties
    public List<PhysicalProperty> getPhysicalProperties(Tissue t);
    //public int addPhysicalProperty(Tissue t, PhysicalProperty p);
    public void updatePhysicalProperty(PhysicalProperty p);
    public void deletePhysicalProperty(PhysicalProperty p); //deletes all the properties associated with this property
    public int getNextPhysicalPropertyId();

    //echogenicity
    public Echogenicity getEchogenicity(PhysicalProperty p);
    public void setEchogenicity(Tissue t, PhysicalProperty p, Echogenicity e);
    public void updateEchogenicity(PhysicalProperty p, Echogenicity e);
       //no delete: delete the physical property instead
    
    //chemical blend
    public ChemicalBlend getChemicalBlend(PhysicalProperty p);
    public void setChemicalBlend(Tissue t, PhysicalProperty p, ChemicalBlend c);
    public void updateChemicalBlend(PhysicalProperty p, ChemicalBlend c);

    //magnetic properties
    public List<MagneticProperty> getMagneticProperties(PhysicalProperty p);
    public void setMagneticProperties(Tissue t, PhysicalProperty p, List<MagneticProperty> mp);
    public void updateMagneticProperties(PhysicalProperty p, List<MagneticProperty> mp);

    public void addMagneticPropertyName(String name);
    public void deleteMagneticPropertyName(String name);
    public List<String> getMagneticPropertyNames();

    //distributions
    public List<Distribution> getDistributions();
    public void addDistribution(Distribution d);
    public void updateDistribution(Distribution d);
    public void deleteDistribution(Distribution d);

    //distribution instances
    public List<DistributionInstance> getDistributionInstances();
    public DistributionInstance getDistributionInstance(int instanceId);
    public void addDistributionInstance(DistributionInstance di);
    public void updateDistributionInstance(DistributionInstance di);
    public void deleteDistributionInstance(DistributionInstance di);

}