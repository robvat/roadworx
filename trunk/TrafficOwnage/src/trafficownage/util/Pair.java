/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package trafficownage.util;

/**
 *
 * @author Gerrit Drost <gerritdrost@gmail.com>
 */
public class Pair<E,F> {
    private E obj1;
    private F obj2;

    public Pair(E obj1, F obj2) {
        this.obj1 = obj1;
        this.obj2 = obj2;
    }

    public E getObject1() {
        return obj1;
    }

    public F getObject2() {
        return obj2;
    }

    public void setObject1(E obj1) {
        this.obj1 = obj1;
    }

    public void setObject2(F obj2) {
        this.obj2 = obj2;
    }
}
