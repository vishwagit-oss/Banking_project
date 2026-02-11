package com.banking.account.config;

import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import static org.springframework.data.mongodb.core.query.Criteria.where;

/**
 * Generates sequential Long IDs for MongoDB documents so we keep the same REST API (e.g. customer Id 1, 2).
 */
@Service
public class MongoSequenceService {

    private static final String SEQUENCE_COLLECTION = "sequences";

    private final MongoTemplate mongoTemplate;

    public MongoSequenceService(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    /**
     * Ensures the sequence is at least (max _id in the collection + 1).
     * Call at startup so IDs stay correct when documents were inserted without the sequence (e.g. seed or manual).
     */
    public void ensureSequenceAtLeast(String sequenceName, String collectionName) {
        var q = new Query().with(org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Direction.DESC, "_id")).limit(1);
        var maxDoc = mongoTemplate.findOne(q, MaxIdDocument.class, collectionName);
        long minSeq = (maxDoc != null && maxDoc.id != null ? maxDoc.id : 0) + 1;
        var existing = mongoTemplate.findById(sequenceName, SequenceDocument.class, SEQUENCE_COLLECTION);
        if (existing == null) {
            mongoTemplate.insert(new SequenceDocument(sequenceName, minSeq), SEQUENCE_COLLECTION);
        } else if (existing.getSeq() < minSeq) {
            mongoTemplate.updateFirst(
                    new Query(where("_id").is(sequenceName)),
                    new Update().set("seq", minSeq),
                    SEQUENCE_COLLECTION
            );
        }
    }

    /** Used only to read max _id from a collection (collection name passed to findOne). */
    public static class MaxIdDocument {
        @org.springframework.data.annotation.Id
        private Long id;
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
    }

    public Long nextSequence(String sequenceName) {
        var result = mongoTemplate.findAndModify(
                new Query(where("_id").is(sequenceName)),
                new Update().inc("seq", 1),
                FindAndModifyOptions.options().returnNew(true).upsert(true),
                SequenceDocument.class,
                SEQUENCE_COLLECTION
        );
        return result != null ? result.getSeq() : 1L;
    }

    public static class SequenceDocument {
        @org.springframework.data.annotation.Id
        private String id;
        private long seq;

        public SequenceDocument() {}
        public SequenceDocument(String id, long seq) { this.id = id; this.seq = seq; }
        public long getSeq() { return seq; }
        public void setSeq(long seq) { this.seq = seq; }
    }
}
