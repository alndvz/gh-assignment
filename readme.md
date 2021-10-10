# GH Banking API Assignment 
By Allan Davies

## Overview
First of all thank you for the opportunity to complete this assignment. I feel
my solution does the job, however it does have a few shortcomings.

My goal was to keep any asynchronous code only where it was required. I didn't 
want it to permeate through the whole code base. Dealing with synchronous code
provides for a more pleasant development experience. 

I used XTDB for persistence, formerly known as Crux. There are probably better
choices, however I have been using XTDB in my side projects, and I enjoyed 
exploring it with this assignment.

## Shortcomings
When using XTDB, you can submit a transaction, the db will then index it, some 
time later, the data will be available for querying. You can also wait for the
transaction to be indexed if you want to.

At present my code will not wait, it will submit then return an updated record 
to the user. In this way I can get updates back to the user faster, at the expense
of inconsistency. This will complicate the user interface, because it will need
to communicate failed transactions to the user at a later time. The API itself,
also ends up more complicated, because it needs to facilitate getting this extra
information to the user.

By adding a `:wait? true` argument to the `db.core/write-many` call, the request
will only return once the transaction has been indexed. This removes the need for
providing for inconsistent state, but slows down the API.

Lastly, my id generator code leaves a lot to be desired. I think an application 
crash could cause duplicated ids to be generated. The most recent IDs are persisted
in memory during application operation, memory usage will steadily increase as 
more keys are added.

## Omissions
* I have not tested what will happen if the application crashes while XTDB is
  trying to catch up on it's indexing.
* I have tested the core functionality, but not added any integration tests
* I have not provided any facilities for tracking failed transactions. 
  (When XTDB's match operation fails)
  
## Load Testing Results
I made use of Jmeter for load testing. I have achieved the goal of 1000 concurrent
requests, however the application response time suffers.

The test I used can be found in this repo, `simple-test.jmx`. I only tested
creating an account and querying an account.

To run: `jmeter -n -t simple-test.jmx -l simple-test-results.jtl`

Machine tested on: 2019 Macbook Pro, 2,6 GHz 6-Core Intel Core i7, 16gb RAM.

**Not waiting for transaction to be indexed**

    summary +  44546 in 00:00:15 = 3009.9/s Avg:   216 Min:     2 Max:   481 Err:     0 (0.00%) Active: 1000 Started: 1000 Finished: 0
    summary +  91926 in 00:00:30 = 3064.5/s Avg:   324 Min:   144 Max:   532 Err:     0 (0.00%) Active: 957 Started: 1000 Finished: 43
    summary = 136472 in 00:00:45 = 3046.3/s Avg:   289 Min:     2 Max:   532 Err:     0 (0.00%)
    summary +  63528 in 00:00:22 = 2846.6/s Avg:   277 Min:     1 Max:   529 Err:     0 (0.00%) Active: 0 Started: 1000 Finished: 1000
    summary = 200000 in 00:01:07 = 2979.9/s Avg:   285 Min:     1 Max:   532 Err:     0 (0.00%)
    Tidying up ...    @ Sun Oct 10 14:57:52 SAST 2021 (1633870672325)
    ... end of run
    
**Waiting for transaction to be indexed (incl. id gen)**

    summary +   8608 in 00:00:13 =  659.7/s Avg:   882 Min:   127 Max:  1772 Err:     0 (0.00%) Active: 1000 Started: 1000 Finished: 0
    summary +  31428 in 00:00:30 = 1047.6/s Avg:   951 Min:   797 Max:  1219 Err:     0 (0.00%) Active: 1000 Started: 1000 Finished: 0
    summary =  40036 in 00:00:43 =  930.0/s Avg:   936 Min:   127 Max:  1772 Err:     0 (0.00%)
    summary +  29458 in 00:00:30 =  981.8/s Avg:  1017 Min:   416 Max:  1835 Err:     0 (0.00%) Active: 1000 Started: 1000 Finished: 0
    summary =  69494 in 00:01:13 =  951.3/s Avg:   971 Min:   127 Max:  1835 Err:     0 (0.00%)
    summary +  23678 in 00:00:30 =  789.2/s Avg:  1261 Min:   904 Max:  1629 Err:     0 (0.00%) Active: 1000 Started: 1000 Finished: 0
    summary =  93172 in 00:01:43 =  904.1/s Avg:  1044 Min:   127 Max:  1835 Err:     0 (0.00%)
    summary +  21941 in 00:00:30 =  731.5/s Avg:  1368 Min:   511 Max:  2105 Err:     0 (0.00%) Active: 1000 Started: 1000 Finished: 0
    summary = 115113 in 00:02:13 =  865.2/s Avg:  1106 Min:   127 Max:  2105 Err:     0 (0.00%)
    summary +  24388 in 00:00:30 =  813.0/s Avg:  1232 Min:  1008 Max:  1828 Err:     0 (0.00%) Active: 1000 Started: 1000 Finished: 0
    summary = 139501 in 00:02:43 =  855.6/s Avg:  1128 Min:   127 Max:  2105 Err:     0 (0.00%)
    summary +  26602 in 00:00:30 =  886.7/s Avg:  1127 Min:   487 Max:  1899 Err:     0 (0.00%) Active: 1000 Started: 1000 Finished: 0
    summary = 166103 in 00:03:13 =  860.4/s Avg:  1128 Min:   127 Max:  2105 Err:     0 (0.00%)
    summary +  25059 in 00:00:30 =  834.8/s Avg:  1195 Min:   973 Max:  1740 Err:     0 (0.00%) Active: 929 Started: 1000 Finished: 71
    summary = 191162 in 00:03:43 =  857.0/s Avg:  1137 Min:   127 Max:  2105 Err:     0 (0.00%)
    summary +   8838 in 00:00:10 =  895.7/s Avg:   858 Min:     0 Max:  1194 Err:     0 (0.00%) Active: 0 Started: 1000 Finished: 1000
    summary = 200000 in 00:03:53 =  858.6/s Avg:  1124 Min:     0 Max:  2105 Err:     0 (0.00%)
    Tidying up ...    @ Sun Oct 10 15:59:39 SAST 2021 (1633874379888)

## Running the application

To run: `clojure -M -m server.core`

To test: `clj -M:test`

To dev, jack in, then run `(start)`

Will listen on port 8080

## Conclusion

I could have done more, however I had to limit what I did to the time I had. 
Thank you again for the opportunity. I hope I get the chance to talk about this test
during an interview.
