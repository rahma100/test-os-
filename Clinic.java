
package sleepingDO;


import java.util.ArrayList;
// To generate a waiting room which supports FIFO
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Semaphore;
//A mutual exclusion lock used to prevent deadlock
import java.util.concurrent.locks.ReentrantLock;    
import java.util.logging.Level;
import java.util.logging.Logger;
import sleepingDO.Patient;
import sleepingDO.Session;

public class Clinic {
    private final ReentrantLock mutex = new ReentrantLock();
    private int waiting_Chairs, num_DO, available_DO;
    private int Total_Examination_done, BackLater_Counter;
    private List<Patient> Patient_List;
    private List<Patient> Patient_BackLater;
    private Semaphore Availabe;
    private Random r = new Random();
    private Session form;
    
    public Clinic(int n_Chairs, int n_DO, int n_Patient, Session form) {
        this.waiting_Chairs = n_Chairs;
        this.num_DO = n_DO;
        this.available_DO = n_DO;
        this.form = form;
        Availabe = new Semaphore(available_DO);//constructor to the semaphore 
        this.Patient_List = new LinkedList<Patient>();
        this.Patient_BackLater = new ArrayList<Patient>(n_Patient);
    }

    

    public int getTotal_Examined_done() {
        return Total_Examination_done;
    }

    public int getBackLater_Counter() {
        return BackLater_Counter;
    }
    
    public void Examination_done(int DO_ID){
        Patient patient;
        
        
        synchronized(Patient_List){
            while (Patient_List.size() == 0) {
                form.Sleep_DO(DO_ID);
                System.out.println("\nDoctor "+DO_ID+" is waiting "
                		+ "for the patient and sleeps in his desk");
                try {
                    Patient_List.wait(); //if the current is choice the wait is decreament and signal is increament
                } catch (InterruptedException ex) {
                    Logger.getLogger(Clinic.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        
            patient = (Patient)((LinkedList<?>)Patient_List).poll();
            System.out.println("Patient "+patient.getPatient_ID()+
            		" finds Doctor available and Examine "
            		+ "the Doctor "+DO_ID);
        }
            int Delay;
            try {
                if (Availabe.tryAcquire() && Patient_List.size() == waiting_Chairs){
                Availabe.acquire();
                }
                form.Busy_DO(DO_ID);
                System.out.println("Doctor "+DO_ID+" Make Examination of "+
            		patient.getPatient_ID());
                
                //return next random_number
                double val = r.nextGaussian() * 2000 + 4000;				
        	Delay = Math.abs((int) Math.round(val));				
        	Thread.sleep(Delay);
                
                System.out.println("\nCompleted Examination of "+
        			patient.getPatient_ID()+" by Doctor " + 
        			DO_ID +" in "+(int)(Delay/1000)+ " seconds.");
                
                //method increments count by 1 and gives the lock to the thread 
                mutex.lock();
                try {
                    //this count 
                    Total_Examination_done++;
                } finally {
                    //method decrements count by 1
                    mutex.unlock();
                }
                
                if (Patient_List.size() > 0) {
                    System.out.println("Doctor "+DO_ID+					
            			" Calls a Patient to enter Clinic ");
                    form.Return_Chair(DO_ID);
                }
                //end of acqire () 
                Availabe.release();
                
            } catch (InterruptedException e) {
            }   
        }
        
        
    
    
    
    public void Enter_Clinic(Patient patient){
        System.out.println("\nPatient "+patient.getPatient_ID()+
        		" tries to enter clinic to Examine at "
        		+patient.getInTime());
        
        synchronized(Patient_List){
            if (Patient_List.size() == waiting_Chairs) {
                
                System.out.println("\nNo chair available "
                		+ "for Patient "+patient.getPatient_ID()+
                		" so Patient leaves and will come back later");
                
                Patient_BackLater.add(patient);
                //method increments count by 1 and gives the lock to the thread 
                mutex.lock();
                try {
                    //this count
                    BackLater_Counter++;
                } finally {
                    //method decrements count by 1
                    mutex.unlock();
                }
                return;
            }
            //Returns the current number of permits available in this semaphore.
            else if (Availabe.availablePermits() > 0 ) {
                ((LinkedList<Patient>)Patient_List).offer(patient);
                //wakes up a single thread 
                Patient_List.notify();
            }
            else{
                try {
                    ((LinkedList<Patient>)Patient_List).offer(patient);
                    form.Take_Chair();
                    System.out.println("All Doctors are busy so Patient "+
                            patient.getPatient_ID()+
                            " takes a chair in the waiting room");
                    
                    if (Patient_List.size() == 1) {
                        Patient_List.notify();
                    }
                } catch (InterruptedException ex) {
                    Logger.getLogger(Clinic.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            
            
        }
    }
    
    public List<Patient> Backlater(){
        return Patient_BackLater;
    }
    
    
    
}
