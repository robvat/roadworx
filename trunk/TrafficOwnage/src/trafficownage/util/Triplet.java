/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package trafficownage.util;

import trafficownage.util.Pair;

/**
 *
 * @author Gerrit Drost <gerritdrost@gmail.com>
 */
public class Triplet<E,F,G> extends Pair<E,F> {
    
    private G obj3;

    public Triplet(E obj1, F obj2, G obj3) {
        super(obj1,obj2);

        this.obj3 = obj3;
    }

    public G getObject3() {
        return obj3;
    }

    public void setObject3(G obj3) {
        this.obj3 = obj3;
    }
}
