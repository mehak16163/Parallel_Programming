import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

class flight{ //flight class
	
	int lmode =-1; //-1 means no lock , 0 means shared lock , 1 means exclusive lock 
	int id ;
	int scount=0;
	int capacity;
	ReentrantLock lock = new ReentrantLock(); //for exclusive locks
	static int count=1;
	ArrayList<passenger> pass = new ArrayList<passenger>(); //passengers of that flight
	public flight(int c){
		id = count;
		count++;
		capacity = c;
	}
}

class passenger{ //passenger class
int lmode = -1; //-1 means no lock, 0 means shared lock , 1 means exclusive lock 
	ReentrantLock lock = new ReentrantLock();
	int scount =0;
	int id;
	static int count=1;
	ArrayList<flight> fl = new ArrayList<flight>(); //flights of that passenger 
	public passenger(){
		id = count;
		count++;
	}
}

abstract class Transaction implements Runnable{ //transaction class which is abstract
	flight_info_db db; //database of the transaction
	boolean mode;
	public Transaction(flight_info_db _db , boolean m ){
		db = _db;
		mode = m;
	}
}

class Reserve extends Transaction{ //Transaction of type reserve
	int f;
	int p;
	public Reserve(flight_info_db _db , int _f , int _p , boolean m){ //constructor of the transaction
		super(_db,m);
		f = _f;
		p = _p;
	}
	public void run() {
		try {
			Thread.sleep(10);
		} catch (InterruptedException e) {
			
		}
		flight _f = db.fl[f];
		passenger _p = db.pass[p];
		if (_f.pass.contains(_p)){
			System.out.println("Passenger "+p+" has already reserved a seat in flight "+f+".");
			return;
		}
		if (mode){
			int i=0;
			while(_p.lmode!=-1 && _f.lmode!= -1 && i<=10000) //checking for shared locks if acquired.
				i++;
			if (i==10001){
				System.out.println("Deadlock on Reserve("+f+","+p+")");
				return;
        }
			_p.lock.lock();
			_f.lock.lock();
			if (_f.pass.size()<_f.capacity){
				System.out.println("Seat reserved for Passenger "+p+" on flight "+ f+".");
				_p.lmode = 1;
				_f.lmode = 1;
				_p.fl.add(_f);
				_f.pass.add(_p);
			}
			else{
				System.out.println("Capacity of flight "+f+" is full. Seat for passenger "+p+" could not be reserved.");
			}
			_p.lock.unlock();
			_f.lock.unlock();
			_p.lmode=-1; _f.lmode=-1;
			//releasing the locks
		}
		else{
			if (_f.pass.size()<_f.capacity){
				_p.fl.add(_f);
				_f.pass.add(_p);
				System.out.println("Seat reserved for Passenger "+p+" on flight "+ f+".");
			}
			else{
				System.out.println("Capacity of flight "+f+" is full. Seat for passenger "+p+" could not be reserved.");
			}
		}
	}
	
}

class My_Flight extends Transaction{
	int id;
	public My_Flight(flight_info_db db , int _id, boolean mode){
		super(db, mode);
		id = _id;
	}
	
	public void run() {
		try {
			Thread.sleep(1);
		} catch (InterruptedException e) {
			
		}
		passenger _p = db.pass[id];// TODO Auto-generated method stub
		if(mode){
    int i=0;
			while(_p.lmode==1 && i<10000){
				i++;
			}
			if(i==10000){
				System.out.println("Deadlock condition on Transaction My_Flight("+id+").");
				return;
			}
			if(_p.lmode==-1 || _p.lmode==0){
				_p.scount++;
				_p.lmode=0;
				System.out.println("The flights for passenger "+id+" are:");
				if(_p.fl.size() == 0)
				{
					System.out.println("No flight");
				}
				else
				{
					for(int j=0;j<_p.fl.size();j++){
						System.out.print(_p.fl.get(j).id+" ");
					}
					System.out.println();
				}
				_p.scount--;
				if(_p.scount==0){
					_p.lmode=-1;
				}
			}
		}
		else{
			System.out.println("The flights for passenger "+id+" are:");
			if(_p.fl.size() == 0)
			{
				System.out.println("No flight");
        }
			else
			{
				for(int j=0;j<_p.fl.size();j++){
					System.out.print(_p.fl.get(j).id+" ");
				}
				System.out.println();
			}
		}
	}
	
}

class Total_Reservations extends Transaction
{
	public Total_Reservations(flight_info_db db, boolean m)
	{
		super(db, m);
	}

	public void run() 
	{
		try {
    Thread.sleep(1);
		} catch (InterruptedException e) {
			
		}
		int sum = 0;
		if(mode)
		{
			for(int i = 1 ; i<db.fl.length ; i++)
			{
				flight f = db.fl[i];
				int j = 0;
				while(f.lmode == 1 && j<10000)
				{
					j++;
				}
				if(j == 10000)
				{
					System.out.println("Deadlock condition on Total_Reservations().");
					return;
				}
				if(f.lmode==-1 || f.lmode==0)
				{
					f.scount++;
					f.lmode=0;
					sum = sum + f.pass.size();
					f.scount--;
					if(f.scount == 0)
					{
						f.lmode = -1;
					}
				}
			}
			System.out.println("Total Reservations made on all flights are: " + sum);
		}
		else{
			for (int i=1;i<db.fl.length;i++){
				sum = sum + db.fl[i].pass.size();
			}
			System.out.println("Total Reservations made on all flights are: " + sum);
		}
	}
}

class Cancel extends Transaction
{
	int f;
  int p;
	public Cancel(flight_info_db _db , int _f , int _p , boolean m)
	{
		super(_db, m);
		f = _f;
		p = _p;
	}
	public void run() 
	{
		try {
			Thread.sleep(1);
		} catch (InterruptedException e) {
			
		}
		flight _f = db.fl[f];
		passenger _p = db.pass[p];
		if(_f.pass.contains(_p) == true) // check if passenger had reserved that flight or not
		{
			if(mode)
			{
				int i=0;
				while(_p.lmode != -1 && _f.lmode != -1 && i<=10000)
				{
					i = i + 1;
				}
				if (i == 10001){
					System.out.println("Deadlock on Cancel("+f+","+p+")");
					return;
				}
				_p.lock.lock();
				_f.lock.lock();
				_p.lmode = 1;
				_f.lmode = 1;
				_p.fl.remove(_f);
				_f.pass.remove(_p);
				_p.lock.unlock();
				_f.lock.unlock();
				System.out.println("Flight "+f+"cancelled for passenger "+p+".");
				_p.lmode = -1;
				_f.lmode = -1;
			}
			else
			{
				_p.fl.remove(_f);
				_f.pass.remove(_p);
				System.out.println("Flight "+f+"cancelled for passenger "+p+".");
			}
		}
else
		{
			System.out.println("Cancellation not done since, passenger " + p + " did not reserve Flight " + f);
		}
	}
}

class Transfer extends Transaction
{
	int f1;
	int f2;
	int p;
	public Transfer(flight_info_db _db , int _f1 , int _f2, int _p , boolean m)
	{
		super(_db, m);
		f1 = _f1;
		f2 = _f2;
		p = _p;
	}
	public void run() {
		try {
			Thread.sleep(1);
		} catch (InterruptedException e) {
			
		}
		flight _f1 = db.fl[f1];
		flight _f2 = db.fl[f2];
		passenger _p = db.pass[p];
		if(_f1.pass.contains(p) == true && _f2.capacity > _f2.pass.size())
		{
			if(mode)
			{
				int i=0;
				while(_p.lmode != -1 && _f1.lmode != -1 && _f2.lmode != -1 && i<=10000)
				{
					i = i + 1;
				}
				if (i == 10001){
					System.out.println("Deadlock on Transfer("+f1+","+f2+","+p+")");
					return;
				}
				_p.lock.lock();
				if(_f1.id<_f2.id) // for avoiding deadlocks
				{
					_f1.lock.lock();
					_f2.lock.lock();
          }
				else
				{
					_f2.lock.lock();
					_f1.lock.lock();
				}
				_p.lmode = 1;
				_f1.lmode = 1;
				_f2.lmode = 1;
				_p.fl.remove(_f1);
				_p.fl.add(_f2);
				_f1.pass.remove(_p);
				_f2.pass.add(_p);
				_p.lock.unlock();
				if(_f1.id<_f2.id)
				{
					_f1.lock.unlock();
					_f2.lock.unlock();
				}
				else
				{
					_f2.lock.unlock();
					_f1.lock.unlock();
				}
				_p.lmode = -1;
				_f1.lmode = -1;
				_f2.lmode = -1;
				System.out.println("Passenger "+p + " transfered from flight "+f1+" to flight "+f2+".");
			}
			else
			{
				_p.fl.remove(_f1);
				_p.fl.add(_f2);
				_f1.pass.remove(_p);
				_f2.pass.add(_p);
				System.out.println("Passenger "+p + " transfered from flight "+f1+" to flight "+f2+".");
			}
		}
		else if (!_f1.pass.contains(_p)){
			System.out.println("No transfer since, No passenger "+p+" on flight "+f1+".");
		}
		else{
			System.out.println("No transfer since, No capacity in flight "+f2+".");
}
	}
}

public class flight_info_db { //database class
	static passenger[] pass; //list of passengers
	static flight[] fl; //list of flights
	static int scount=0;
	static int lmode =-1; //-1 for no lock , 0 for shared, 1 for exclusive. 
	public static void main(String[] args) throws NumberFormatException, IOException, InterruptedException{
		BufferedReader rd = new BufferedReader(new InputStreamReader(System.in));
		System.out.println("Enter no. of flights: "); // taking user input
		int  flights = Integer.parseInt(rd.readLine());
		System.out.println("Enter no. of passengers: ");
		int passengers = Integer.parseInt(rd.readLine());
		pass = new passenger[passengers+1];
		fl = new flight[flights+1];
		System.out.println("Enter flight capacities:");
		String[] cap = rd.readLine().split(" ");
		for (int i=1;i<=passengers; i++){
			pass[i] = new passenger();
		}
		for (int i=1;i<=flights;i++){
			fl[i]= new flight(Integer.parseInt(cap[i-1]));
		}
		System.out.println("Enter Mode of execution:\n1. Serially\n2. Concurrently");
		int m = Integer.parseInt(rd.readLine());
		boolean mode=false;
		if (m==2)
			mode = true;
		flight_info_db db = new flight_info_db();
		System.out.println("Enter no. of transactions: ");
		int t = Integer.parseInt(rd.readLine());
		Transaction[] tran = new Transaction[t];
		
		for(int i=0;i<t;i++){
			Random rint = new Random();
			int tid = rint.nextInt(100)+1; // generating a random integer for selecting the transaction operation
			if (tid>=0 && tid<=20){
				int f = rint.nextInt(flights)+1;
				int p = rint.nextInt(passengers)+1; // passenger and flight id is generated randomly
				tran[i] = new Reserve(db,f,p,mode);
			}
			else if (tid>=21 && tid<=40){
      int f = rint.nextInt(flights)+1;
				int p = rint.nextInt(passengers)+1;
				tran[i] = new Cancel(db,f,p,mode);
			}
			else if (tid>=41 && tid<=60){
				int p = rint.nextInt(passengers)+1;
				tran[i] = new My_Flight(db , p , mode);
			}
			else if (tid>=61 && tid<=80){
				tran[i] = new Total_Reservations(db , mode);
			}
			else{
				int f = rint.nextInt(flights)+1;
				int p = rint.nextInt(passengers)+1;
				int f2 = rint.nextInt(flights)+1;
				tran[i] = new Transfer(db , f, f2 , p ,mode);
			}
		}
		long startTime; 
    	long endTime; 
   		startTime = System.currentTimeMillis(); // start time of execution
		if (mode){
			ExecutorService exec = Executors.newFixedThreadPool(5); //creating thread pool for concurrency.
			for (int i=0;i<t;i++){
				exec.execute(tran[i]);
			}
			if(!exec.isTerminated()){
				exec.shutdown();
				exec.awaitTermination(10,TimeUnit.SECONDS);
			}
		}
		else{ // serial execution
			for(int i=0;i<t;i++){
				tran[i].run();
			}
		}	
		endTime = System.currentTimeMillis(); // end time of execution
    	double final_time = (endTime - startTime)/1000.0;
		Thread.sleep(5000);
    	System.out.println("Time Elapsed is: " + final_time);       	
	}
}
