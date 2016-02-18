/*
 *  Andre Cavalcante e Rafael Mendonca
 *  Copyright UFAM 2015-2016
 */
package test.siape;

import eps.Debug;
import eps.MRAInfo;
import eps.MRAServices;
import eps.Product;
import eps.SkillTemplate;
import eps.Util;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Product Agent
 *
 * @author andre
 */
public class Anagram extends Product {

    private final String anagram;
    private final List<PlanItem> tablePlan;

    public Anagram(String anagram) {
        this.anagram = anagram;
        this.tablePlan = new ArrayList<>();
    }

    @Override
    protected MRAInfo getMRAInfo() {
        MRAInfo mraInfo = new MRAInfo();
        mraInfo.setName(this.getLocalName());
        mraInfo.setProperties(new String[0]);
        mraInfo.setSkills(Util.fromSkill(getSkills()));
        return mraInfo;
    }
/**
 * Method initialized after anagram agent created.
 */
    @Override
    protected void produce() {
        SkillTemplate st;
        MRAInfo achwInfo;
        String[] letras;

        System.out.println("Running production... ");

        try {

            //Define o AcHw
            achwInfo = new MRAInfo();
            achwInfo.setName("achw");
            achwInfo.setProperties(new String[0]);
            achwInfo.setSkills(new SkillTemplate[0]);

            //Pega as letras
            st = new SkillTemplate("GetLetters", "void", new String[0]);
            String result = MRAServices.executeRemoteSkill(this, achwInfo, st);
            letras = result.split(",");

            //Cria o plano
            for (int pos = 0; pos < anagram.length(); pos++) {
                String letra = anagram.substring(pos, pos + 1);
                for (int mod = 0; mod < letras.length; mod++) {
                    if (letra.equals(letras[mod])) {
                        tablePlan.add(new PlanItem(mod, pos + 1, letra));
                    }
                }
            }
            Collections.sort(tablePlan);
            for (int i = 0; i < tablePlan.size() - 1; i++) {
                for (int j = i+1; j < tablePlan.size(); j++) {
                    if (tablePlan.get(j).mod == tablePlan.get(i).mod) {
                        tablePlan.get(j).pos = tablePlan.get(j).pos - tablePlan.get(i).pos;
                    }
                }
            }

            //Executa o plano
            st = new SkillTemplate("MoveToStart", "void", new String[0]);
            MRAServices.executeRemoteSkill(this, achwInfo, st);

            for (PlanItem item : tablePlan) {
                st = new SkillTemplate("MoveTo", "void", new String[]{"int", "int"});
                st.setArgsValues(new String[]{
                    String.valueOf(item.mod),
                    String.valueOf(item.pos)
                });
                MRAServices.executeRemoteSkill(this, achwInfo, st);
                System.out.println(String.format("Letra: %s | MoveTo(%d, %d)", item.letter, item.mod, item.pos));

                st = new SkillTemplate("Stamp", "void", new String[]{"int"});
                st.setArgsValues(new String[]{String.valueOf(item.mod)});
                MRAServices.executeRemoteSkill(this, achwInfo, st);
            }

            st = new SkillTemplate("MoveToEnd", "void", new String[0]);
            MRAServices.executeRemoteSkill(this, achwInfo, st);
        } catch (Exception ex) {
            Debug.printError(ex.toString());
        }
    }

}
/**
 * Auxiliary class for order index in planItem
 * @author Rafael
 */
class PlanItem implements Comparable<PlanItem> {

    int mod;
    int pos;
    String letter;

    public PlanItem(int mod, int pos, String letter) {
        this.mod = mod;
        this.pos = pos;
        this.letter = letter;
    }

    @Override
    public int compareTo(PlanItem other) {
        return this.mod - other.mod;
    }

    @Override
    public String toString() {
        return letter + "/" + mod + "/" + pos;
    }

}
