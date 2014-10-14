package pl.allegro.jdd;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import org.javers.core.Javers;
import org.javers.core.JaversBuilder;
import org.javers.core.diff.Change;
import org.javers.core.diff.Diff;
import org.javers.core.diff.changetype.NewObject;
import org.javers.core.diff.changetype.ObjectRemoved;
import org.javers.core.diff.changetype.PropertyChange;

import pl.allegro.jdd.Changes;
import pl.allegro.jdd.Employee;

public class StructureDiff {

	
	private static final String SALARY_PROPERTY_NAME = "salary";
	
	
    public Changes calculate(Employee oldCTO, Employee newCTO){
        checkNotNull(oldCTO);
        checkNotNull(newCTO);
        Javers javers = JaversBuilder.javers().build();
                
        Diff javersDiff = javers.compare(flattenEmployeeStructure(oldCTO), flattenEmployeeStructure(newCTO));

        java.util.Map<String, List<Change>> changesBucketsMap = javersDiff.getChanges().stream().collect(Collectors.groupingBy(this::categorizeChanges));
        List<Employee> fired = getEmployeesFromChanges(changesBucketsMap.get("delete"));
        List<Employee> hired = getEmployeesFromChanges(changesBucketsMap.get("new"));
        List<Employee> changed = getEmployeesFromChanges(changesBucketsMap.get("changedSalary"));
        
        //changes may contain also some of the created and removed
        changed.removeAll(fired);
        changed.removeAll(hired);
        return new Changes(fired,hired,changed);
    }
    
    private List<Employee> getEmployeesFromChanges(List<Change> changesList){
    	if(null == changesList || changesList.isEmpty()){
    		return Collections.emptyList();
    	}
    	return changesList.stream().map(change->{return (Employee)(change.getAffectedCdo());}).collect(Collectors.toList());
    }
    
    //FIXME ugly code, should be improved
	private String categorizeChanges(Change change){
		if(change instanceof PropertyChange){
			if(((PropertyChange)change).getProperty().getName().equals(SALARY_PROPERTY_NAME)){
			return "changedSalary";
			}
		}else if (change instanceof NewObject){
			return "new";
		}else if(change instanceof ObjectRemoved){
			return "delete";
		}
		return "other";
	}
	
	/*
	 * FIXME Flattening employee tree to only 2 levels, as JaVers has problems with deep structures
	 */
	private Employee flattenEmployeeStructure(Employee sourceEmployee){
		Employee boss = new Employee(sourceEmployee.getName(),sourceEmployee.getSalary());
		LinkedList<Employee> temporaryList = new LinkedList<>();
		sourceEmployee.getSubordinates().forEach(temporaryList::add);
			while(!temporaryList.isEmpty()){
				Employee subordinate =temporaryList.pollFirst();
				boss.addSubordinate(new Employee(subordinate.getName(),subordinate.getSalary()));
				subordinate.getSubordinates().forEach(temporaryList::add);
			}
		
		return boss;
	}
	
}
