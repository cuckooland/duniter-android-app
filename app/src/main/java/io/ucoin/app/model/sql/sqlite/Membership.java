package io.ucoin.app.model.sql.sqlite;

import android.content.ContentValues;
import android.content.Context;

import io.ucoin.app.UcoinUris;
import io.ucoin.app.enumeration.MembershipState;
import io.ucoin.app.enumeration.MembershipType;
import io.ucoin.app.model.UcoinIdentity;
import io.ucoin.app.model.UcoinMembership;
import io.ucoin.app.sqlite.SQLiteTable;
import io.ucoin.app.sqlite.SQLiteView;

public class Membership extends Row
        implements UcoinMembership {

    public Membership(Context context, Long membershipId) {
        super(context, UcoinUris.MEMBERSHIP_URI, membershipId);
    }

    @Override
    public Long identityId() {
        return getLong(SQLiteView.Membership.IDENTITY_ID);
    }

    @Override
    public Long version() {
        return getLong(SQLiteView.Membership.VERSION);
    }

    @Override
    public MembershipType type() {
        return MembershipType.valueOf(getString(SQLiteView.Membership.TYPE));
    }
    @Override
    public Long blockNumber() {
        return getLong(SQLiteView.Membership.BLOCK_NUMBER);
    }

    @Override
    public String blockHash() {
        return getString(SQLiteView.Membership.BLOCK_HASH);
    }

    @Override
    public Long time() {
        return getLong(SQLiteView.Membership.TIME);
    }

    @Override
    public Long expirationTime() {
        return getLong(SQLiteView.Membership.EXPIRATION_TIME);
    }

    @Override
    public Boolean expired() {
        return getBoolean(SQLiteView.Membership.EXPIRED);
    }

    @Override
    public MembershipState state() {
        return MembershipState.valueOf(getString(SQLiteView.Membership.STATE));
    }

    @Override
    public UcoinIdentity identity() {
        return new Identity(mContext, identityId());
    }

    @Override
    public void setState(MembershipState state) {
        ContentValues values = new ContentValues();
        values.put(SQLiteTable.Membership.STATE, state.name());
        update(values);
    }

    @Override
    public String toString() {
        return "Membership id=" + id() + "\n" +
                "identity_id=" + identityId() + "\n" +
                "type=" + type().name() + "\n" +
                "block_number=" + blockNumber() + "\n" +
                "block_hash=" + blockHash();
    }
}