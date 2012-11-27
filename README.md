HDFS B+Tree
===========

A naive implementation of B+tree (partially) on hdfs. It's even not fully tested. :P

It uses a local file as a temporary storage. Once the tree is finished (no further inserting and deleting actions), 
you can copy it to hdfs. Afterwards the reading actions happends on hdfs, but no modification is allowed.

The 'origin' B+tree code of this implementation is extracted from someone's baseballdb code, but I cannot remember
where I downloaded it. 