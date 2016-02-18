/*
 *  Andre Cavalcante e Rafael Mendonca
 *  Copyright UFAM 2015-2016
 */
package eps;

/**
 * Generic Product
 * @author Rafael
 */
public abstract class Product extends MRA {

    private final Skill[] skills;
    
    public Product() {
        skills = new Skill[] {
          new Skill(this, "Produce", "void", new String[0]) {
            @Override
            public void execute() throws SkillExecuteException {
                produce();
            }
          }  
        };
    }
    
    /**
     * Implement this to enable the plan production 
     */
    protected abstract void produce();
    
    @Override
    protected abstract MRAInfo getMRAInfo();

    @Override
    protected Skill[] getSkills() {
        return skills;
    }
    
}
