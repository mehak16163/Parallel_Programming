
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author gupta
 */
public class pascal extends RecursiveAction {
    int n;
    int k;
    BigInteger ans = BigInteger.valueOf(0);
    public pascal(int n, int k){
        this.n = n;
        this.k =k;
    }
    @Override
    public void compute(){
        if (this.n==0 || this.k==0 || (this.n==this.k)){
            ans =BigInteger.valueOf(1);
            return;
        }
        pascal left =new pascal(n-1,k-1);
        pascal right = new pascal(n-1,k);
        left.fork();
        right.compute();
        left.join();
        ans = left.ans.add(right.ans);
    }
    public static void main(String[] args) throws IOException{
        BufferedReader r = new BufferedReader(new InputStreamReader(System.in));
        System.out.println("Enter threadpool size:");
        int s = Integer.parseInt(r.readLine());
        System.out.println("Enter n:");
        int n = Integer.parseInt(r.readLine());
        System.out.println("Enter k:");
        int k = Integer.parseInt(r.readLine());
        pascal p = new pascal(n,k);
        ForkJoinPool pool = new ForkJoinPool(s);
        long start = System.currentTimeMillis();
        pool.invoke(p);
        long end = System.currentTimeMillis();
        System.out.println(p.ans);
        System.out.println(end-start+" milliseconds");
        
    }
    
}
