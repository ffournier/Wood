package com.tonytangandroid.wood

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey

@Entity(tableName = "Leaf")
class Leaf {
    @JvmField
    @Ignore
    var searchKey: String? = null

    @JvmField
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0

    @JvmField
    @ColumnInfo(name = "createAt")
    var createAt: Long = 0

    @JvmField
    @ColumnInfo(name = "tag")
    var tag: String? = null

    @JvmField
    @ColumnInfo(name = "priority")
    var priority: Int = 0

    @ColumnInfo(name = "length")
    private var length = 0

    @ColumnInfo(name = "body", typeAffinity = ColumnInfo.TEXT)
    private var body: String? = null

    fun length(): Int {
        return length
    }

    fun setLength(length: Int) {
        this.length = length
    }

    fun body(): String? {
        return body
    }

    fun setBody(body: String?) {
        this.body = body
    }
}
