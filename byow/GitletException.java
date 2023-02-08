package byow.Core;

/** General exception indicating a Gitlet error.  For fatal errors, the
 *  result of .getMessage() is the error message to be printed.
 *  @author P. N. Hilfinger
 * @source File copied over from proj2 folder with author being P. N. Hilfinger
 * listed above. File was brought over due to its usage by the same author in the
 * Utils file.
 */
class GitletException extends RuntimeException {


    /** A GitletException with no message. */
    GitletException() {
        super();
    }

    /** A GitletException MSG as its message. */
    GitletException(String msg) {
        super(msg);
    }

}

