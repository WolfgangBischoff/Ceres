package Core.Utils;

class Token
    {
        String text;
        public static Token of(String s)
        {
            return new Token(s);
        }
        public Token(String text)
        {
            this.text = text;
        }

        @Override
        public String toString()
        {
            return "["+text+"]";
        }

    }