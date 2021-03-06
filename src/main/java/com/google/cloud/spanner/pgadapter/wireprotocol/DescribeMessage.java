// Copyright 2020 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.cloud.spanner.pgadapter.wireprotocol;

import com.google.cloud.spanner.pgadapter.ConnectionHandler;
import com.google.cloud.spanner.pgadapter.metadata.DescribePortalMetadata;
import com.google.cloud.spanner.pgadapter.metadata.DescribeStatementMetadata;
import com.google.cloud.spanner.pgadapter.statements.IntermediateStatement;
import java.io.DataInputStream;

/**
 * Calls describe on a portal or prepared statement.
 */
public class DescribeMessage extends WireMessage {

  private PreparedType type;
  private String name;
  private IntermediateStatement statement;

  public DescribeMessage(ConnectionHandler connection, DataInputStream input) throws Exception {
    super(connection, input);
    this.type = PreparedType.prepareType((char) input.readUnsignedByte());
    this.remainder = 5;
    this.name = this.read(input);
    if (this.type == PreparedType.Portal) {
      this.statement = this.connection.getPortal(this.name);
    } else {
      this.statement = this.connection.getStatement(this.name);
    }
  }

  @Override
  public void send() throws Exception {
    if (this.type == PreparedType.Portal) {
      this.connection.handleDescribePortal(
          this.statement,
          (DescribePortalMetadata) this.statement.describe());
    } else {
      this.connection.handleDescribeStatement(
          (DescribeStatementMetadata) this.statement.describe());
    }
  }

  public String getName() {
    return this.name;
  }

}
