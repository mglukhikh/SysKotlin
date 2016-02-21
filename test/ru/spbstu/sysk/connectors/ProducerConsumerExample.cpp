#include <systemc.h>
#include <ctime>

class write_if : virtual public sc_interface
{
   public:
     virtual void write(char) = 0;
     virtual void reset() = 0;
};

class read_if : virtual public sc_interface
{
   public:
     virtual void read(char &) = 0;
     virtual int num_available() = 0;
};

class fifo : public sc_channel, public write_if, public read_if
{
   public:
     fifo(sc_module_name name) : sc_channel(name), num_elements(0), first(0) {}

     void write(char c) {
       if (num_elements == max)
         wait(read_event);

       data[(first + num_elements) % max] = c;
       ++ num_elements;
       write_event.notify();
     }

     void read(char &c){
       if (num_elements == 0)
         wait(write_event);

       c = data[first];
       -- num_elements;
       first = (first + 1) % max;
       read_event.notify();
     }

     void reset() { num_elements = first = 0; }

     int num_available() { return num_elements;}

   private:
     enum e { max = 100 };
     char data[max];
     int num_elements, first;
     sc_event write_event, read_event;
};

const long int QEMITS = 100000001;
const long int CUTOFF = 1000000;

char randomChar() {
    return (char)(rand() % 95 + 32);    
}

class producer : public sc_module
{
   public:
     sc_port<write_if> out;

     SC_HAS_PROCESS(producer);

     producer(sc_module_name name) : sc_module(name)
     {
       SC_THREAD(main);
     }

     void main()
     {
        long int qEmits = 0;
        while (qEmits < QEMITS) {
            if (qEmits % CUTOFF == 0) {
                cout<<"I am in producer \n";
            }
            out->write(randomChar());
            qEmits++;
        }
     }
};

class consumer : public sc_module
{
   public:
     sc_port<read_if> in;

     SC_HAS_PROCESS(consumer);

     consumer(sc_module_name name) : sc_module(name)
     {
       SC_THREAD(main);
     }

     void main()
     {
       long int qEmits = 0;
       char c;
       cout << endl << endl;

       while (true) {
         in->read(c);
         if (qEmits % CUTOFF == 0) {
            cout << qEmits << ": I am in consumer " << c << endl << flush;
         }
         qEmits++;
       }
     }
};

class top : public sc_module
{
   public:
     fifo *fifo_inst;
     producer *prod_inst;
     consumer *cons_inst;

     top(sc_module_name name) : sc_module(name)
     {
       fifo_inst = new fifo("Fifo1");

       prod_inst = new producer("Producer1");
       prod_inst->out(*fifo_inst);

       cons_inst = new consumer("Consumer1");
       cons_inst->in(*fifo_inst);
     }
};

int sc_main (int, char *[]) {
   top top1("Top1");
   time_t res = clock();
   sc_start();
   time_t delay = clock() - res;
   cout << delay / 1000000 << "s " << (delay % 1000000) / 1000 << "ms" << endl;
   // 9s 407ms
   // 9s 772ms
   // 9s 376ms
   // 9s 364ms
   // 9s 180ms
   return 0;
}
